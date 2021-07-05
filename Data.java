import java.io.*;
import java.util.*;

/**
 * This class provides the basic database services. It uses two
 * other support classes: DataInfo and FieldInfo.
 *
 * @version 1.1  17-Nov-1997
 */
public class Data {
    private static final byte LIVE_RECORD = 0;
    private static final byte DELETED_RECORD = 1;
    private static final int MAGIC = 0xC0C0BABE;

    private static final String UNEXPECTED =
    "Data: Unexpected database access problem";

    private FieldInfo [] description;
    private int headerLen;
    private int recordLen = 1;
    private int recordCount;
    private RandomAccessFile db;
    final static char sc = 'A';

    /**
    * This constructor opens an existing database given the name
    * of the disk file containing it.
    *
    * @param String dbname The name of the database file to open.
    * @exception java.io.IOException
    */
    public Data(String dbname) throws IOException {
        File f = new File(dbname);
        if (f.exists() && f.canRead() && f.canWrite()) {
            db = new RandomAccessFile(f, "rw");
            headerLen = db.readInt();
            int nFields= db.readInt();
            recordCount = db.readInt();
            description = new FieldInfo[nFields];
            for (int i=0; i<nFields; i++) {
                description[i] = new FieldInfo(db.readUTF(), db.readInt());
                System.out.println(description[i].getName());
                recordLen += description[i].getLength();
            }

            System.out.println("filePointer = ");
            System.out.println(db.getFilePointer());

            if (db.readInt() != MAGIC) {
                throw new IOException("Data: corrupted database file.  " +
                "Magic not found in file " + dbname);
            }

            System.out.println("filePointer = ");
            System.out.println(db.getFilePointer());


            if (db.getFilePointer() != headerLen) {
                throw new IOException("Data: corrupted database file.  " +
                "Header length incorrect in file " + dbname);
            }
        } else {
            throw new IOException("Data: request to open non-existant or " +
                                  "inaccessible file" + dbname);
        }
    }

    /**
    * This constructor creates a new database file, using the name
    * provided for the disk file and using the FieldInfo array to
    * describe the field names and sizes that should be created.
    *
    * @param String dbname The name of the database file to open.
    * @param FieldInfo[] fields The list of fields for the schema of
    *          this database.
    * @exception IOException Thrown if cannot create database file.
    */
    public Data(String dbname, FieldInfo[] fields) throws IOException {
        File f = new File(dbname);
        if (!f.exists()) {
            db = new RandomAccessFile(f, "rw");
            db.writeInt(0);  // filler for header length
            db.writeInt(fields.length);
            recordCount = 0;
            db.writeInt(recordCount);

            for (int i = 0; i < fields.length; i++) {
            db.writeUTF(fields[i].getName());
            db.writeInt(fields[i].getLength());
            recordLen += fields[i].getLength();
            }
            description = fields;
            db.writeInt(MAGIC);
            headerLen = (int)db.getFilePointer();
            db.seek(0);
            db.writeInt(headerLen);
        } else {
            throw new IOException("Data: request to create pre-existing " +
                                  "file" + dbname);
        }
    }

    /**
    * This method returns a description of the database schema, as an
    * array of FieldInfo objects.
    *
    * @return FieldInfo[] The array of FieldInfo objects that comprise
    *          the schema to this database.
    */
    public FieldInfo [] getFieldInfo() {
        return description;
    }

    /**
    * Gets the number of records stored in the database.
    */
    public int getRecordCount() { return recordCount; }

    /**
    * Gets a requested record from the database based on record number.
    * @param recNum The number of the record to read (first record is 1).
    * @return DataInfo for the record or null if the record has been marked for
    *    deletion.
    * @exception DatabaseException Thrown if database file cannot be accessed.
    */
    public synchronized DataInfo getRecord(int recNum) throws DatabaseException {
        try {
           if (recNum<1) {
              throw new DatabaseException("Record number must be greater than 1");
           }

            seek(recNum);
            
            String records[] = readRecord();
            if (records == null) {
               return null;
            }

            return new DataInfo(recNum, description, records);
        } catch(IOException ex) {
            throw new DatabaseException(UNEXPECTED);
        }
    }

    /**
    * This method searches the database for an entry which has a first
    * field which exactly matches the string supplied. If the required
    * record cannot be found, this method returns null. For this
    * assignment, the key field is the record number field.
    *
    * @param toMatch The key field value to match upon for
    *           a successful find.
    * @return DataInfo The matching record.
    * @exception DatabaseException Thrown when database file could not be accessed.
    */
    public synchronized DataInfo find(String toMatch) throws DatabaseException {
        invariant();
        try {
            seek(1);

            DataInfo rv = null;
            String [] values = null;
            boolean found = false;
            int r;

            for (r = 1; r <= recordCount; r++) {
                values = readRecord();

                if ((values != null) && (values[0].equals(toMatch))) {
                    found = true;
                    break;
                }
            }

            if (found) {
                rv = new DataInfo(r, description, values);
            }
            return rv;
        }  catch (IOException e) {
            throw new DatabaseException(UNEXPECTED + e);
        }
    }

public DataInfo[] criteriaFind(String criteria) throws DatabaseException {
    final DataInfo[] NO_RECORD_FOUND = new DataInfo[0];
    StringTokenizer tokenizer = new StringTokenizer(criteria, ",='");
    int noOfTokens = tokenizer.countTokens(); /* no of tokens in criteria */
    /* if no of tokens are odd , or if tokens are zero */
    if ( (noOfTokens % 2 != 0) || (noOfTokens == 0)) {
        return NO_RECORD_FOUND; /* No record found */
    }

    /* fieldName holds field names given in criteria */
    String[] fieldName = new String[noOfTokens
        / 2]; /* fieldValue holds field values given in criteria */
    String[] fieldValue = new String[noOfTokens / 2];

    for (int i = 0, j = noOfTokens / 2; i < j; i++) {
        fieldName[i] = tokenizer.nextToken().trim();
        fieldValue[i] = tokenizer.nextToken();
        System.out.print("fieldName = ");
        System.out.print(fieldName[i]);
        System.out.print(" ");
        System.out.print("fieldValue = ");
        System.out.println(fieldValue[i]);
    }

    for (int i = 0; i < fieldName.length; i++) {
        boolean found = false;
        for (int j = 0; j < description.length; j++) {
            if (fieldName[i].equals(description[j].getName())) {
                found = true;
            }
        }
        if (! (found)) {
            return NO_RECORD_FOUND;
        }
    }

    DataInfo rv = null;
    String[] values = null;
    FieldInfo[] fields = null;

    List dataInfoObjects = new ArrayList();
    List valuesContainer = new ArrayList(recordCount);

    synchronized (this) { // lock this object to block other thread

        for (int i = 1; i <= recordCount; i++) {
            rv = getRecord(i); /* get the DataInfo object */

        if (fields == null) /* field names are same for all recs  */ {
            fields = rv.getFields(); /* get FieldInfo object. */
        }
            values = rv.getValues(); /* get Values */
            valuesContainer.add(values);
        }
    }

    for (int i = 0; i < recordCount; i++) {

            if (fieldValue[0].equals(((String[]) valuesContainer.get(i))[1]) &&
                fieldValue[1].equals(((String[]) valuesContainer.get(i))[2])) {
                dataInfoObjects.add(getRecord(i+1));
            }
            if (fieldValue[0].equals("*") &&
                fieldValue[1].equals(((String[]) valuesContainer.get(i))[2])) {
                dataInfoObjects.add(getRecord(i+1));
            }
            if (fieldValue[0].equals(((String[]) valuesContainer.get(i))[1]) &&
                fieldValue[1].equals("*")) {
                dataInfoObjects.add(getRecord(i+1));
            }
            if (fieldValue[0].equals("*") &&
                fieldValue[1].equals("*")) {
                dataInfoObjects.add(getRecord(i+1));
            }                               
    }

    return (DataInfo[]) dataInfoObjects.toArray( (DataInfo[])new DataInfo[0]);
}

    /**
    * This method adds a new record to the database. The array of
    * strings must have exactly the same number of elements as the
    * field count of the database schema, otherwise a RuntimeException
    * is issued. The first field, the key, must be unique in the
    * database or a RuntimeException is thrown.
    *
    * @param newData The elements of the record to add.
    * @exception DatabaseException Attempted to add a duplicate key or
    *        database file could not be accessed.
    */
    public synchronized void add(String [] newData) throws DatabaseException {
        invariant();
        if (find(newData[0]) != null) {
            throw new DatabaseException("Attempt to add a duplicate key");
        }

        try {
            seek(++recordCount);
            writeRecord(newData);
            db.seek(8);
            db.writeInt(recordCount);
        }
        catch (IOException e) {
            throw new DatabaseException(UNEXPECTED + e);
        }
    }

    /**
    * This method updates the record specified by the record number
    * field in the DataInfo argument. The fields are all modified
    * to reflect the values in that argument. If the key field
    * specified in the argument matches any record other than the
    * one indicated by the record number of the argument, then a
    * RuntimeException is thrown.
    *
    * @param newData The updated record to modify.
    * @exception DatabaseException Thrown if attempting to add a duplicate
    *       key.
    */
    public synchronized void modify(DataInfo newData) throws DatabaseException {
        invariant();
        DataInfo test = find((newData.getValues())[0]);
        if ((test != null) &&
            (test.getRecordNumber()!=newData.getRecordNumber()))
        {
            throw new DatabaseException("Attempt to create a "+
                                        "duplicate key by modification");
        }

        try {
            seek(newData.getRecordNumber());
            writeRecord(newData.getValues());
        } catch (IOException e) {
            throw new DatabaseException(UNEXPECTED + e);
        }
    }

    /**
    * This method deletes the record referred to by the record
    * number in the DataInfo argument.
    *
    * @param DataInfo newData The record to delete.
    * @exception DatabaseException Thrown if database cannot be accessed.
    */
    public synchronized void delete(DataInfo toDelete) throws DatabaseException {
        invariant();
        try {
            seek(toDelete.getRecordNumber());
            db.write(DELETED_RECORD);
        } catch (IOException e) {
            throw new DatabaseException(UNEXPECTED + e);
        }
    }

    /**
    * This method closes the database, flushing any outstanding
    * writes at the same time. Any attempt to access the
    * database after this results in a IOException.
    */
    public synchronized void close() {
        try {
            db.close();
        } catch (IOException e) {}

        db = null;
    }

    protected void finalize() {
        if (db != null) {
            close();
        }
    }

    /**
    * Reads a record from the current cursor position of the underlying random
    * access file.
    * @return The array of strings that make up a database record.
    * @exception IOException Generated if the RandomAccessFile cannot read from
    *        the database file.
    */
    private synchronized String[] readRecord() throws IOException {
        int offset = 1;
        String [] rv = null;
        byte [] buffer = new byte[recordLen];

        db.read(buffer);
        if (buffer[0] == LIVE_RECORD) {
            rv = new String[description.length];
            for (int i = 0; i < description.length; i++) {
                rv[i] = new String(buffer, 0, offset, description[i].getLength());
                offset += description[i].getLength();
            }
        }
        return rv;
    }

    /**
    * Writes a new record to the database using the current location of the
    * underlying random access file.
    * @param newData An array of strings in the database specified order.
    * @exception IOException Generated if the RandomAccessFile cannot write to
    *        the file or the wrong number of fields are specified.
    */
    private void writeRecord(String[] newData) throws IOException {
        if (newData.length != description.length) {
            throw new IOException("Data: Wrong number of fields in writeRecord() " +
                                  newData.length + " given, " +
                                  description.length + " required");
        }

        int size, space, toCopy;
        byte [] buffer = new byte[recordLen];
        buffer[0] = LIVE_RECORD;
        int offset = 1;

        for (int i = 0; i < description.length; i++) {
            space = description[i].getLength();
            size = newData[i].length();
            toCopy = (size <= space) ? size : space;

            newData[i].getBytes(0, toCopy, buffer, offset);
            offset += space;
        }

        db.write(buffer);
    }

    /**
    * Moves the current database record pointer to the specified record.
    * @param recno The record number to position the cursor.
    * @exception IOException If the record position is invalid.
    */
    private synchronized void seek(int recno) throws IOException {
        db.seek(headerLen + (recordLen * (recno - 1)));
    }

    /**
    * Lock the requested record. If the argument is -1, lock the whole
    * database. This method blocks until the lock succeeds. No timeouts
    * are defined for this.
    * @param recno The record number to lock.
    * @exception IOException If the record position is invalid.
    */
    public void lock(int record) throws IOException {
    }

    /**
    * Unlock the requested record. Ignored if the caller does not have
    * a current lock on the requested record.
    */
    public void unlock(int record) {
    }

    /**
    * Ensures that the database structure is valid.
    * @exception RuntimeException If structure has become corrupted.
    */
    protected final synchronized void invariant() {
        boolean ok = false;
        try {
            System.out.print("db.length() = ");
            System.out.println(db.length());
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        System.out.print("headerLen plus (recordLen * recordCount) = ");
        System.out.println(headerLen + (recordLen * recordCount));
        
        System.out.print("headerLen = ");
        System.out.println(headerLen);
        System.out.print("recordLen = ");
        System.out.println(recordLen);
        System.out.print("recordCount = ");
        System.out.println(recordCount);
        System.out.print("recordLen * recordCount = ");
        System.out.println(recordLen * recordCount);

        try {
            if (db.length() == headerLen + (recordLen * recordCount)) {
                ok = true;
            }
        } catch (Exception e) {
            throw new RuntimeException(UNEXPECTED + e);
        }

        if (!ok) {
            throw new RuntimeException("Data: Internal error");
        }
    }
}
