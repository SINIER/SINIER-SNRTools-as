package com.bluetooth.modbus.snrtools2.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "Cmd".
*/
public class CmdDao extends AbstractDao<Cmd, Long> {

    public static final String TABLENAME = "Cmd";

    /**
     * Properties of entity Cmd.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property HexNo = new Property(1, String.class, "hexNo", false, "hexNo");
        public final static Property CmdName = new Property(2, String.class, "cmdName", false, "cmdName");
        public final static Property Ext = new Property(3, String.class, "ext", false, "ext");
        public final static Property CmdPwd = new Property(4, String.class, "cmdPwd", false, "cmdPwd");
        public final static Property BtAddress = new Property(5, String.class, "btAddress", false, "btAddress");
    }


    public CmdDao(DaoConfig config) {
        super(config);
    }
    
    public CmdDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"Cmd\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"hexNo\" TEXT," + // 1: hexNo
                "\"cmdName\" TEXT," + // 2: cmdName
                "\"ext\" TEXT," + // 3: ext
                "\"cmdPwd\" TEXT," + // 4: cmdPwd
                "\"btAddress\" TEXT);"); // 5: btAddress
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"Cmd\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, Cmd entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String hexNo = entity.getHexNo();
        if (hexNo != null) {
            stmt.bindString(2, hexNo);
        }
 
        String cmdName = entity.getCmdName();
        if (cmdName != null) {
            stmt.bindString(3, cmdName);
        }
 
        String ext = entity.getExt();
        if (ext != null) {
            stmt.bindString(4, ext);
        }
 
        String cmdPwd = entity.getCmdPwd();
        if (cmdPwd != null) {
            stmt.bindString(5, cmdPwd);
        }
 
        String btAddress = entity.getBtAddress();
        if (btAddress != null) {
            stmt.bindString(6, btAddress);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, Cmd entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String hexNo = entity.getHexNo();
        if (hexNo != null) {
            stmt.bindString(2, hexNo);
        }
 
        String cmdName = entity.getCmdName();
        if (cmdName != null) {
            stmt.bindString(3, cmdName);
        }
 
        String ext = entity.getExt();
        if (ext != null) {
            stmt.bindString(4, ext);
        }
 
        String cmdPwd = entity.getCmdPwd();
        if (cmdPwd != null) {
            stmt.bindString(5, cmdPwd);
        }
 
        String btAddress = entity.getBtAddress();
        if (btAddress != null) {
            stmt.bindString(6, btAddress);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public Cmd readEntity(Cursor cursor, int offset) {
        Cmd entity = new Cmd( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // hexNo
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // cmdName
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // ext
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // cmdPwd
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5) // btAddress
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, Cmd entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setHexNo(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setCmdName(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setExt(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setCmdPwd(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setBtAddress(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(Cmd entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(Cmd entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(Cmd entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
