package it.paolodurante.droneitalia;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import java.io.File;
import java.io.FileNotFoundException;
public class FlightShareProvider extends ContentProvider {
 @Override public boolean onCreate(){return true;}
 private File file(Uri uri)throws FileNotFoundException{
 String name=uri.getLastPathSegment();
 if(name==null||!name.matches("flight-[0-9]+\\.jpg")||uri.getPathSegments().size()!=1)throw new FileNotFoundException();
 File file=new File(new File(getContext().getCacheDir(),"flight-shares"),name);
 if(!file.isFile())throw new FileNotFoundException();return file;
 }
 @Override public String getType(Uri uri){return "image/jpeg";}
 @Override public ParcelFileDescriptor openFile(Uri uri,String mode)throws FileNotFoundException{
 if(!"r".equals(mode))throw new FileNotFoundException("Read only");
 return ParcelFileDescriptor.open(file(uri),ParcelFileDescriptor.MODE_READ_ONLY);
 }
 @Override public Cursor query(Uri uri,String[] projection,String selection,String[] args,String sort){
 try{
 File file=file(uri);String[] columns=projection==null?new String[]{OpenableColumns.DISPLAY_NAME,OpenableColumns.SIZE}:projection;
 MatrixCursor cursor=new MatrixCursor(columns);Object[] values=new Object[columns.length];
 for(int i=0;i<columns.length;i++)values[i]=OpenableColumns.DISPLAY_NAME.equals(columns[i])?"Drone-Italia-volo.jpg":OpenableColumns.SIZE.equals(columns[i])?file.length():null;
 cursor.addRow(values);return cursor;
 }catch(FileNotFoundException e){return null;}
 }
 @Override public Uri insert(Uri uri,ContentValues values){throw new UnsupportedOperationException();}
 @Override public int delete(Uri uri,String selection,String[] args){return 0;}
 @Override public int update(Uri uri,ContentValues values,String selection,String[] args){return 0;}
}
