import express from "express";
import Stripe from "stripe";
import Database from "better-sqlite3";
import { timingSafeEqual } from "node:crypto";

const app = express();
app.disable("x-powered-by");
const port = Number(process.env.PORT || 8787);
const trialDays = 5;
const priceCents = 499;
const stripeKey = process.env.STRIPE_SECRET_KEY || "";
const webhookSecret = process.env.STRIPE_WEBHOOK_SECRET || "";
const adminToken = process.env.TEST_CHECKOUT_TOKEN || "";
const baseUrl = process.env.PUBLIC_BASE_URL || "";
const stripeTest = stripeKey.startsWith("sk_test_");
const stripe = stripeTest ? new Stripe(stripeKey) : null;
const testCheckoutReady = Boolean(stripe && webhookSecret.startsWith("whsec_") &&
  adminToken.length >= 32 && /^https:\/\//.test(baseUrl) &&
  !baseUrl.includes("example.com"));

const db = new Database(process.env.DB_PATH || "./licenses.sqlite");
db.pragma("journal_mode = WAL");
db.exec(`CREATE TABLE IF NOT EXISTS stripe_events (
 event_id TEXT PRIMARY KEY,
 event_type TEXT NOT NULL,
 session_id TEXT,
 received_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
)`);

function tokenMatches(input) {
 if (typeof input !== "string" || !adminToken || input.length !== adminToken.length) return false;
 return timingSafeEqual(Buffer.from(input), Buffer.from(adminToken));
}
function noStore(_req, res, next) { res.set("Cache-Control", "no-store"); next(); }

// Raw body MUST precede JSON parser for Stripe signature verification.
app.post("/v1/webhooks/stripe", express.raw({ type: "application/json", limit: "128kb" }), (req, res) => {
 if (!stripe || !webhookSecret.startsWith("whsec_")) return res.sendStatus(503);
 let event;
 try {
  event = stripe.webhooks.constructEvent(req.body, req.headers["stripe-signature"], webhookSecret);
 } catch (_error) {
  return res.status(400).json({ error: "invalid_signature" });
 }
 const session = event.data?.object;
 const sessionId = typeof session?.id === "string" ? session.id : null;
 db.prepare("INSERT OR IGNORE INTO stripe_events (event_id,event_type,session_id) VALUES (?,?,?)")
   .run(event.id, event.type, sessionId);
 // Recording a paid event is NOT granting a license. Requires verified account binding,
 // payment reconciliation, refund handling, and an entitlement service.
 return res.json({ received: true });
});
app.use(express.json({ limit: "16kb" }));
app.use(noStore);

app.get("/health", (_req, res) => res.json({ status: "ok", service: "drone-italia-licensing" }));
app.get("/v1/config", (_req, res) => res.json({
 productId: "drone_italia_full", trialDays, currency: "EUR", priceCents,
 checkoutEnabled: false, enforcementEnabled: false,
 stripeSandboxReady: testCheckoutReady, paymentMethodsPlanned: ["stripe", "paypal"]
}));

// Administrator-only Stripe test checkout. No production key or actual license is accepted.
// Do not embed TEST_CHECKOUT_TOKEN in the APK; this route is only for manual server-side testing.
app.post("/v1/admin/stripe-test-checkout", async (req, res) => {
 if (!testCheckoutReady) return res.status(503).json({ error: "sandbox_not_configured" });
 if (!tokenMatches(req.get("x-test-checkout-token"))) return res.sendStatus(403);
 try {
  const session = await stripe.checkout.sessions.create({
   mode: "payment",
   line_items: [{ price_data: { currency: "eur", unit_amount: priceCents,
    product_data: { name: "Drone Italia — sblocco definitivo (TEST)" } }, quantity: 1 }],
   success_url: baseUrl + "/payment-test/success?session_id={CHECKOUT_SESSION_ID}",
   cancel_url: baseUrl + "/payment-test/cancel",
   metadata: { product_id: "drone_italia_full", environment: "sandbox" }
  });
  res.json({ url: session.url, sessionId: session.id, sandbox: true });
 } catch (_error) {
  res.status(502).json({ error: "stripe_checkout_failed" });
 }
});

app.use((_req, res) => res.status(404).json({ error: "not_found" }));
app.listen(port, "127.0.0.1", () => console.log("Drone Italia licensing sandbox on localhost:" + port));
