package com.bluetooth.modbus.snrtools2.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.bluetooth.modbus.snrtools2.manager.AppStaticVar;

import org.greenrobot.greendao.identityscope.IdentityScopeType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cchen on 2017/8/10.
 */

public class DBManager {

    private CmdDao cmdDao;
    private OfflineStringDao offlineStringDao;
    private ParamGroupDao paramGroupDao;
    private ParamDao paramDao;
    private VarDao varDao;
    private MainDao mainDao;
    private ValueDao valueDao;
    private Context mContext;
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private SQLiteDatabase mDb;

    private static DBManager instance;

    public static DBManager getInstance()
    {
        if (instance == null)
        {
            instance = new DBManager();
        }
        return instance;
    }

    private DBManager()
    {
        mContext = AppStaticVar.mApplication;
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(mContext, "db", null);
//        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(mContext, Environment.getExternalStoragePublicDirectory("")+"/sinierdb.db", null);
        mDb = helper.getWritableDatabase();
        daoMaster = new DaoMaster(mDb);
        daoSession = daoMaster.newSession(IdentityScopeType.None);
        cmdDao = daoSession.getCmdDao();
        offlineStringDao = daoSession.getOfflineStringDao();
        paramGroupDao = daoSession.getParamGroupDao();
        paramDao = daoSession.getParamDao();
        varDao = daoSession.getVarDao();
        mainDao = daoSession.getMainDao();
        valueDao = daoSession.getValueDao();
    }

    public void clearSession(){
        daoSession.clear();
    }

    public List<OfflineString> getAllOfflineString(){
        synchronized (this){
            return offlineStringDao.queryBuilder().where(OfflineStringDao.Properties.BtAddress.eq(AppStaticVar.mProductInfo.crcModel)).list();
        }
    }

    public void saveOfflineString(OfflineString offlineString){
        synchronized (this){
            offlineString.setBtAddress(AppStaticVar.mProductInfo.crcModel);
            if(offlineString.getId() == null) {
                OfflineString temp = offlineStringDao.queryBuilder().where(OfflineStringDao.Properties.HexNo.eq(offlineString.getHexNo()), OfflineStringDao.Properties.BtAddress.eq(offlineString.getBtAddress())).unique();
                if (temp != null) {
                    offlineString.setId(temp.getId());
                }
            }
            offlineStringDao.insertOrReplace(offlineString);
        }
    }

    public void updateOfflineStringCrcModel(String oldCrcModel,String crcModel){
        synchronized (this){
            //UPDATE table_name
//            SET column1=value1,column2=value2,...
//            WHERE some_column=some_value;
            String sql = "UPDATE "+OfflineStringDao.TABLENAME+" SET "+OfflineStringDao.Properties.BtAddress.columnName+" =? "
                    +" WHERE "+OfflineStringDao.Properties.BtAddress.columnName+" =? ";
            offlineStringDao.getDatabase().execSQL(sql,new String[]{crcModel,oldCrcModel});
        }
    }

    public void saveCmd(Cmd cmd){
        synchronized (this){
            cmd.setBtAddress(AppStaticVar.mProductInfo.crcModel);
            if(cmd.getId() == null) {
                Cmd temp = cmdDao.queryBuilder().where(CmdDao.Properties.HexNo.eq(cmd.getHexNo()), CmdDao.Properties.BtAddress.eq(cmd.getBtAddress())).unique();
                if (temp != null) {
                    cmd.setId(temp.getId());
                }
            }
            cmdDao.insertOrReplace(cmd);
        }
    }

    public void saveVar(Var var){
        synchronized (this){
            var.setBtAddress(AppStaticVar.mProductInfo.crcModel);
            if(var.getId() == null) {
                Var temp = varDao.queryBuilder().where(VarDao.Properties.HexNo.eq(var.getHexNo()), VarDao.Properties.BtAddress.eq(var.getBtAddress())).unique();
                if (temp != null) {
                    var.setId(temp.getId());
                }
            }
            varDao.insertOrReplace(var);
        }
    }

    public Var getVar(String key){
        synchronized (this) {
            Var var = varDao.queryBuilder().where(VarDao.Properties.HexNo.eq(key),VarDao.Properties.BtAddress.eq(AppStaticVar.mProductInfo.crcModel)).unique();
            return var;
        }
    }

    public List<Var> getAllVar(){
        synchronized (this) {
            List<Var> var = varDao.queryBuilder().where(VarDao.Properties.BtAddress.eq(AppStaticVar.mProductInfo.crcModel)).list();
            return var;
        }
    }

    public void saveValue(Value value){
        synchronized (this){
            value.setBtAddress(AppStaticVar.mProductInfo.crcModel);
            if(value.getId() == null) {
                Value temp = valueDao.queryBuilder().where(ValueDao.Properties.Key.eq(value.getKey()), ValueDao.Properties.BtAddress.eq(value.getBtAddress())).unique();
                if (temp != null) {
                    value.setId(temp.getId());
                }
            }
            valueDao.insertOrReplace(value);
        }
    }

    public Value getValue(String key){
        synchronized (this) {
            Value var = valueDao.queryBuilder().where(ValueDao.Properties.Key.eq(key),ValueDao.Properties.BtAddress.eq(AppStaticVar.mProductInfo.crcModel)).unique();
            return var;
        }
    }
    public List<Value> getAllValue(){
        synchronized (this) {
            List<Value> var = valueDao.queryBuilder().where(ValueDao.Properties.BtAddress.eq(AppStaticVar.mProductInfo.crcModel)).list();
            return var==null?new ArrayList<Value>():var;
        }
    }

    public void clearParam(){
        synchronized (this){
            paramDao.queryBuilder().where(ParamDao.Properties.BtAddress.eq(AppStaticVar.mProductInfo.crcModel)).buildDelete().executeDeleteWithoutDetachingEntities();
        }
    }

    public void clearParamGroup(){
        synchronized (this){
            paramGroupDao.queryBuilder().where(ParamGroupDao.Properties.BtAddress.eq(AppStaticVar.mProductInfo.crcModel)).buildDelete().executeDeleteWithoutDetachingEntities();
        }
    }

    public void clearCmd(){
        synchronized (this){
            cmdDao.queryBuilder().where(CmdDao.Properties.BtAddress.eq(AppStaticVar.mProductInfo.crcModel)).buildDelete().executeDeleteWithoutDetachingEntities();
        }
    }

    public void clearStr(){
        synchronized (this){
            offlineStringDao.queryBuilder().where(OfflineStringDao.Properties.BtAddress.eq(AppStaticVar.mProductInfo.crcModel)).buildDelete().executeDeleteWithoutDetachingEntities();
        }
    }

    public void clearValue(){
        synchronized (this){
            valueDao.queryBuilder().where(ValueDao.Properties.BtAddress.eq(AppStaticVar.mProductInfo.crcModel)).buildDelete().executeDeleteWithoutDetachingEntities();
        }
    }

    public void clearVar(){
        synchronized (this){
            varDao.queryBuilder().where(VarDao.Properties.BtAddress.eq(AppStaticVar.mProductInfo.crcModel)).buildDelete().executeDeleteWithoutDetachingEntities();
        }
    }

    public void clearMain(){
        synchronized (this){
            mainDao.queryBuilder().where(MainDao.Properties.BtAddress.eq(AppStaticVar.mProductInfo.crcModel)).buildDelete().executeDeleteWithoutDetachingEntities();
        }
    }

    public void saveMain(Main main){
        synchronized (this){
            main.setBtAddress(AppStaticVar.mProductInfo.crcModel);
            if(main.getId() == null) {
                Main temp = mainDao.queryBuilder().where(MainDao.Properties.Type.eq(main.getType()),MainDao.Properties.HexNo.eq(main.getHexNo()), MainDao.Properties.BtAddress.eq(main.getBtAddress())).unique();
                if (temp != null) {
                    main.setId(temp.getId());
                }
            }
            mainDao.insertOrReplace(main);
        }
    }

    public List<Main> getMainList(){
        synchronized (this) {
            List<Main> list = mainDao.queryBuilder().where(MainDao.Properties.BtAddress.eq(AppStaticVar.mProductInfo.crcModel)).list();
            return list==null?new ArrayList<Main>():list;
        }
    }

    public List<ParamGroup> getParamGroups(String level){
        synchronized (this){
            List<ParamGroup> result = paramGroupDao.queryBuilder().where(ParamGroupDao.Properties.Level.le(level),ParamGroupDao.Properties.BtAddress.eq(AppStaticVar.mProductInfo.crcModel)).build().list();
            return result==null?new ArrayList<ParamGroup>():result;
        }
    }

    public List<ParamGroup> getAllParamGroups(){
        synchronized (this){
            List<ParamGroup> result = paramGroupDao.queryBuilder().where(ParamGroupDao.Properties.BtAddress.eq(AppStaticVar.mProductInfo.crcModel)).build().list();
            return result==null?new ArrayList<ParamGroup>():result;
        }
    }

    public List<Cmd> getCmds(String level){
        synchronized (this){
            List<Cmd> result = cmdDao.queryBuilder().where(CmdDao.Properties.CmdPwd.eq(level),CmdDao.Properties.BtAddress.eq(AppStaticVar.mProductInfo.crcModel)).build().list();
            return result==null?new ArrayList<Cmd>():result;
        }
    }

    public List<Cmd> getAllCmds(){
        synchronized (this){
            List<Cmd> result = cmdDao.queryBuilder().where(CmdDao.Properties.BtAddress.eq(AppStaticVar.mProductInfo.crcModel)).build().list();
            return result==null?new ArrayList<Cmd>():result;
        }
    }

    public void saveParamGroup(ParamGroup paramGroup){
        synchronized (this){
            paramGroup.setBtAddress(AppStaticVar.mProductInfo.crcModel);
            if(paramGroup.getId() == null) {
                ParamGroup temp = paramGroupDao.queryBuilder().where(ParamGroupDao.Properties.HexNo.eq(paramGroup.getHexNo()), ParamGroupDao.Properties.BtAddress.eq(paramGroup.getBtAddress())).unique();
                if (temp != null) {
                    paramGroup.setId(temp.getId());
                }
            }
            paramGroupDao.insertOrReplace(paramGroup);
        }
    }

    public List<Param> getParams(String groupHexNo){
        synchronized (this){
            List<Param> result = paramDao.queryBuilder().where(ParamDao.Properties.ParamGroupHexNo.eq(groupHexNo),ParamDao.Properties.BtAddress.eq(AppStaticVar.mProductInfo.crcModel)).build().list();
            return result==null?new ArrayList<Param>():result;
        }
    }

    public List<Param> getAllParams(){
        synchronized (this){
            List<Param> result = paramDao.queryBuilder().where(ParamDao.Properties.BtAddress.eq(AppStaticVar.mProductInfo.crcModel)).build().list();
            return result==null?new ArrayList<Param>():result;
        }
    }

    public Param getParam(String paramHexNo){
        synchronized (this){
            try {
                return paramDao.queryBuilder().where(ParamDao.Properties.HexNo.eq(paramHexNo),ParamDao.Properties.BtAddress.eq(AppStaticVar.mProductInfo.crcModel)).unique();
            }catch (Exception e){
                return null;
            }
        }
    }

    public void saveParam(Param param){
        synchronized (this){
            param.setBtAddress(AppStaticVar.mProductInfo.crcModel);
            if(param.getId() == null) {
                Param temp = paramDao.queryBuilder().where(ParamDao.Properties.HexNo.eq(param.getHexNo()), ParamDao.Properties.BtAddress.eq(param.getBtAddress())).unique();
                if (temp != null) {
                    param.setId(temp.getId());
                }
            }
            paramDao.insertOrReplace(param);
        }
    }

    public List<OfflineString> getAllStrs(){
        synchronized (this){
            return offlineStringDao.queryBuilder().where(OfflineStringDao.Properties.BtAddress.eq(AppStaticVar.mProductInfo.crcModel)).list();
        }
    }

    public String getStr(String key){
        synchronized (this) {
            try {
                OfflineString offlineString = offlineStringDao.queryBuilder().where(OfflineStringDao.Properties.HexNo.eq(key),OfflineStringDao.Properties.BtAddress.eq(AppStaticVar.mProductInfo.crcModel)).unique();
                if(offlineString == null){
                    return "";
                }
                return (!AppStaticVar.isChinese ? offlineString.getStringEn():offlineString.getStringZh())
                        .replaceAll("~","³")
                        .replaceAll("\\^","℃")
                        .replaceAll("`","²");
            }catch (Exception e){
                return "";
            }
        }
    }
}
