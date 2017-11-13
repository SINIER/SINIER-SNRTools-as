package classgenerator;

import org.junit.Test;

import org.greenrobot.greendao.generator.DaoGenerator;
import org.greenrobot.greendao.generator.Entity;
import org.greenrobot.greendao.generator.Schema;

public class OfflineGenerator {

    private static Entity string;
    private static Entity cmd;
    private static Entity paramGroup;
    private static Entity param;
    private static Entity var;
    private static Entity main;
    private static Entity value;

    @Test
    public void testMinimalSchema() throws Exception
    {
		Schema schema = new Schema(3, "com.bluetooth.modbus.snrtools2.db");

		addString(schema);
		addCmd(schema);
		addParamGroup(schema);
		addParam(schema);
		addVar(schema);
		addMain(schema);
		addValue(schema);
		new DaoGenerator().generateAll(schema, "C:\\work\\github\\SINIER-SNRTools-as\\app\\src\\main\\java");
	}

    /** 键值对，替代sharepreference */
    private static void addValue(Schema schema)
    {
        value = schema.addEntity("Value");
        value.setDbName("Value");
        value.setJavaDoc("键值对，替代sharepreference");
        value.addIdProperty().autoincrement().primaryKey();
        value.addStringProperty("key").dbName("key").javaDocGetterAndSetter("主键");
        value.addStringProperty("value").dbName("value").javaDocGetterAndSetter("值");
        value.addStringProperty("btAddress").dbName("btAddress").javaDocGetterAndSetter("连接设备地址");
        value.implementsSerializable();//实现序列化接口
        value.setHasKeepSections(true);//生成的类可以添加自主代码
    }

    private static void addString(Schema schema)
    {
        string = schema.addEntity("OfflineString");
        string.setDbName("OfflineString");
        string.setJavaDoc("离线字符串");
        string.addIdProperty().autoincrement().primaryKey();
        string.addStringProperty("hexNo").dbName("hexNo").javaDocGetterAndSetter("十六进制编号（例：0000）");
        string.addStringProperty("stringZh").dbName("stringZh").javaDocGetterAndSetter("中文");
        string.addStringProperty("stringEn").dbName("stringEn").javaDocGetterAndSetter("英文");
        string.addStringProperty("btAddress").dbName("btAddress").javaDocGetterAndSetter("连接设备地址");
        string.implementsSerializable();//实现序列化接口
        string.setHasKeepSections(true);//生成的类可以添加自主代码
    }

    private static void addCmd(Schema schema)
    {
        cmd = schema.addEntity("Cmd");
        cmd.setDbName("Cmd");
        cmd.setJavaDoc("离线命令");
        cmd.addIdProperty().autoincrement().primaryKey();
        cmd.addStringProperty("hexNo").dbName("hexNo").javaDocGetterAndSetter("十六进制编号（例：0000）");
        cmd.addStringProperty("cmdName").dbName("cmdName").javaDocGetterAndSetter("命令名称");
        cmd.addStringProperty("ext").dbName("ext").javaDocGetterAndSetter("预留");
        cmd.addStringProperty("cmdPwd").dbName("cmdPwd").javaDocGetterAndSetter("命令密码");
        cmd.addStringProperty("btAddress").dbName("btAddress").javaDocGetterAndSetter("连接设备地址");
        cmd.implementsSerializable();//实现序列化接口
        cmd.setHasKeepSections(true);//生成的类可以添加自主代码
    }

    private static void addVar(Schema schema)
    {
        var = schema.addEntity("Var");
        var.setDbName("Var");
        var.setJavaDoc("实时变量配置");
        var.addIdProperty().autoincrement().primaryKey();
        var.addStringProperty("hexNo").dbName("hexNo").javaDocGetterAndSetter("十六进制编号（例：0000）");
        var.addStringProperty("type").dbName("type").javaDocGetterAndSetter("变量类型");
        var.addStringProperty("count").dbName("count").javaDocGetterAndSetter("变量选项数量/小数位");
        var.addStringProperty("unit").dbName("unit").javaDocGetterAndSetter("变量单位/选项字符串指针");
        var.addStringProperty("btAddress").dbName("btAddress").javaDocGetterAndSetter("连接设备地址");
        var.implementsSerializable();//实现序列化接口
        var.setHasKeepSections(true);//生成的类可以添加自主代码
    }

    private static void addMain(Schema schema)
    {
        main = schema.addEntity("Main");
        main.setDbName("Main");
        main.setJavaDoc("主界面配置");
        main.addIdProperty().autoincrement().primaryKey();
        main.addStringProperty("type").dbName("type").javaDocGetterAndSetter("显示内容类别 0-变量，1-参数，2-图标，3-字符串，4-波形");
        main.addStringProperty("fontSize").dbName("fontSize").javaDocGetterAndSetter("字体大小 2-大字体，1-小字体，0-普通字体");
        main.addStringProperty("gravity").dbName("gravity").javaDocGetterAndSetter("对齐（2-中间，1-右对齐，0-左对齐）");
        main.addStringProperty("count").dbName("count").javaDocGetterAndSetter("小数点位数（0-7位）");
        main.addStringProperty("x").dbName("x").javaDocGetterAndSetter("x坐标 显示行坐标 0-7行（以8像素为一个行单位）");
        main.addStringProperty("y").dbName("y").javaDocGetterAndSetter("y坐标 显示列坐标 0-127列");
        main.addStringProperty("width").dbName("width").javaDocGetterAndSetter("显示区域宽度 1-128");
        main.addStringProperty("height").dbName("height").javaDocGetterAndSetter("显示区域高度 预留未使用 ");
        main.addStringProperty("hexNo").dbName("hexNo").javaDocGetterAndSetter("数据索引号,十六进制编号（例：0000）");
        main.addStringProperty("value").dbName("value").javaDocGetterAndSetter("数据值");
        main.addStringProperty("btAddress").dbName("btAddress").javaDocGetterAndSetter("连接设备地址");
        main.implementsSerializable();//实现序列化接口
        main.setHasKeepSections(true);//生成的类可以添加自主代码
    }

    private static void addParamGroup(Schema schema)
    {
        paramGroup = schema.addEntity("ParamGroup");
        paramGroup.setDbName("ParamGroup");
        paramGroup.setJavaDoc("参数组");
        paramGroup.addIdProperty().autoincrement().primaryKey();
        paramGroup.addStringProperty("hexNo").dbName("hexNo").javaDocGetterAndSetter("十六进制编号（例：0000）");
        paramGroup.addStringProperty("name").dbName("name").javaDocGetterAndSetter("名称");
        paramGroup.addStringProperty("level").dbName("level").javaDocGetterAndSetter("等级");
        paramGroup.addStringProperty("btAddress").dbName("btAddress").javaDocGetterAndSetter("连接设备地址");
        paramGroup.implementsSerializable();//实现序列化接口
        paramGroup.setHasKeepSections(true);//生成的类可以添加自主代码
    }

    private static void addParam(Schema schema)
    {
        param = schema.addEntity("Param");
        param.setDbName("Param");
        param.setJavaDoc("参数组");
        param.addIdProperty().autoincrement().primaryKey();
        param.addStringProperty("hexNo").dbName("hexNo").javaDocGetterAndSetter("十六进制编号（例：0000）");
        param.addStringProperty("paramGroupHexNo").dbName("paramGroupHexNo").javaDocGetterAndSetter("参数组编号");
        param.addStringProperty("type").dbName("type").javaDocGetterAndSetter("数据类型");
        param.addStringProperty("name").dbName("name").javaDocGetterAndSetter("名称");
        param.addStringProperty("value").dbName("value").javaDocGetterAndSetter("值");
        param.addStringProperty("valueDisplay").dbName("valueDisplay").javaDocGetterAndSetter("显示给客户看的值");
        param.addStringProperty("linkVariable").dbName("linkVariable").javaDocGetterAndSetter("关联变量");
        param.addStringProperty("count").dbName("count").javaDocGetterAndSetter("选项数量/小数位数");
        param.addStringProperty("unit").dbName("unit").javaDocGetterAndSetter("选项字符串索引/单位");
        param.addStringProperty("max").dbName("max").javaDocGetterAndSetter("最大值");
        param.addStringProperty("min").dbName("min").javaDocGetterAndSetter("最小值");
        param.addStringProperty("btAddress").dbName("btAddress").javaDocGetterAndSetter("连接设备地址");
        param.implementsSerializable();//实现序列化接口
        param.setHasKeepSections(true);//生成的类可以添加自主代码
    }
}
