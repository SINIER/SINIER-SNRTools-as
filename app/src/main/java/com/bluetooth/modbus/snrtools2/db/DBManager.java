package com.bluetooth.modbus.snrtools2.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.ab.util.AbSharedUtil;
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

    public List<OfflineString> getAllOfflineString(){
        synchronized (this){
            return offlineStringDao.loadAll();
        }
    }

    public void saveOfflineString(OfflineString offlineString){
        synchronized (this){
            offlineStringDao.insertOrReplace(offlineString);
        }
    }

    public void saveCmd(Cmd cmd){
        synchronized (this){
            cmdDao.insertOrReplace(cmd);
        }
    }

    public void saveVar(Var var){
        synchronized (this){
            varDao.insertOrReplace(var);
        }
    }

    public Var getVar(String key){
        synchronized (this) {
            Var var = varDao.queryBuilder().where(VarDao.Properties.HexNo.eq(key)).unique();
            return var;
        }
    }

    public void saveValue(Value value){
        synchronized (this){
            valueDao.insertOrReplace(value);
        }
    }

    public Value getValue(String key){
        synchronized (this) {
            Value var = valueDao.queryBuilder().where(ValueDao.Properties.Key.eq(key)).unique();
            return var;
        }
    }

    public void clearMain(){
        synchronized (this){
            mainDao.deleteAll();
        }
    }

    public void saveMain(Main main){
        synchronized (this){
            mainDao.insertOrReplace(main);
        }
    }

    public List<Main> getMainList(){
        synchronized (this) {
            List<Main> list = mainDao.loadAll();
            return list==null?new ArrayList<Main>():list;
        }
    }

    public List<ParamGroup> getParamGroups(String level){
        synchronized (this){
            List<ParamGroup> result = paramGroupDao.queryBuilder().where(ParamGroupDao.Properties.Level.le(level)).build().list();
            return result==null?new ArrayList<ParamGroup>():result;
        }
    }

    public List<Cmd> getCmds(String level){
        synchronized (this){
            List<Cmd> result = cmdDao.queryBuilder().where(CmdDao.Properties.CmdPwd.eq(level)).build().list();
            return result==null?new ArrayList<Cmd>():result;
        }
    }

    public void saveParamGroup(ParamGroup paramGroup){
        synchronized (this){
            paramGroupDao.insertOrReplace(paramGroup);
        }
    }

    public List<Param> getParams(String groupHexNo){
        synchronized (this){
            List<Param> result = paramDao.queryBuilder().where(ParamDao.Properties.ParamGroupHexNo.eq(groupHexNo)).build().list();
            return result==null?new ArrayList<Param>():result;
        }
    }

    public Param getParam(String paramHexNo){
        synchronized (this){
            return paramDao.queryBuilder().where(ParamDao.Properties.HexNo.eq(paramHexNo)).unique();
        }
    }

    public void saveParam(Param param){
        synchronized (this){
            paramDao.insertOrReplace(param);
        }
    }

    public List<OfflineString> getAllStrs(){
        synchronized (this){
            return offlineStringDao.loadAll();
        }
    }

    public String getStr(String key){
        synchronized (this) {
            OfflineString offlineString = offlineStringDao.queryBuilder().where(OfflineStringDao.Properties.HexNo.eq(key)).unique();
            if(offlineString == null){
                return "";
            }
            return (AppStaticVar.isEnglish ? offlineString.getStringEn():offlineString.getStringZh())
                    .replaceAll("~","³")
                    .replaceAll("\\^","℃")
                    .replaceAll("`","²");
        }
    }
}
