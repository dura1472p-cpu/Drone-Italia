import express from "express";

const app = express();
app.disable("x-powered-by");
const port = Number(process.env.PORT || 8787);
const trialDays = 5;
const priceCents = 499;

app.get("/health", (_req, res) => {
  res.set("Cache-Control", "no-store");
  res.json({ status: "ok", service: "drone-italia-licensing" });
});

app.get("/v1/config", (_req, res) => {
  res.set("Cache-Control", "no-store");
  res.json({
    productId: "drone_italia_full",
    trialDays,
    currency: "EUR",
    priceCents,
    checkoutEnabled: false,
    enforcementEnabled: false,
    paymentMethodsPlanned: ["stripe", "paypal"]
  });
});

// Deliberatamente nessun endpoint di acquisto o attivazione licenza:
// prima servono autenticazione, webhook verificati e database.
app.use((_req, res) => res.status(404).json({ error: "not_found" }));
app.listen(port, "127.0.0.1", () => {
  console.log("Drone Italia licensing preparatory service on localhost:" + port);
});
