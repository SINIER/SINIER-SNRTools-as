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

    @Test
    public void testMinimalSchema() throws Exception
    {
		Schema schema = new Schema(1, "com.bluetooth.modbus.snrtools2.db");

		addString(schema);
		addCmd(schema);
		addParamGroup(schema);
		addParam(schema);
		addVar(schema);
		new DaoGenerator().generateAll(schema, "C:\\work\\github\\SINIER-SNRTools-as\\app\\src\\main\\java");
	}

    /** 离线字符串 */
    private static void addString(Schema schema)
    {
        string = schema.addEntity("OfflineString");
        string.setDbName("OfflineString");
        /** 十六进制编号（例：0000） */
        string.addStringProperty("hexNo").dbName("hexNo").primaryKey();
        /** 中文 */
        string.addStringProperty("stringZh").dbName("stringZh");
        /** 英文 */
        string.addStringProperty("stringEn").dbName("stringEn");
        string.implementsSerializable();//实现序列化接口
        string.setHasKeepSections(true);//生成的类可以添加自主代码
    }

    /** 离线命令 */
    private static void addCmd(Schema schema)
    {
        cmd = schema.addEntity("Cmd");
        cmd.setDbName("Cmd");
        /** 十六进制编号（例：0000） */
        cmd.addStringProperty("hexNo").dbName("hexNo").primaryKey();
        /** 命令名称 */
        cmd.addStringProperty("cmdName").dbName("cmdName");
        /** 预留 */
        cmd.addStringProperty("ext").dbName("ext");
        /** 命令密码 */
        cmd.addStringProperty("cmdPwd").dbName("cmdPwd");
        cmd.implementsSerializable();//实现序列化接口
        cmd.setHasKeepSections(true);//生成的类可以添加自主代码
    }

    /** 实时变量配置 */
    private static void addVar(Schema schema)
    {
        var = schema.addEntity("Var");
        var.setDbName("Var");
        /** 十六进制编号（例：0000） */
        var.addStringProperty("hexNo").dbName("hexNo").primaryKey();
        /** 变量类型 */
        var.addStringProperty("type").dbName("type");
        /** 变量选项数量/小数位 */
        var.addStringProperty("count").dbName("count");
        /** 变量单位/选项字符串指针 */
        var.addStringProperty("unit").dbName("unit");
        var.implementsSerializable();//实现序列化接口
        var.setHasKeepSections(true);//生成的类可以添加自主代码
    }

    /** 参数组 */
    private static void addParamGroup(Schema schema)
    {
        paramGroup = schema.addEntity("ParamGroup");
        paramGroup.setDbName("ParamGroup");
        /** 十六进制编号（例：0000） */
        paramGroup.addStringProperty("hexNo").dbName("hexNo").primaryKey();
        /** 名称 */
        paramGroup.addStringProperty("name").dbName("name");
        /** 等级 */
        paramGroup.addStringProperty("level").dbName("level");
        paramGroup.implementsSerializable();//实现序列化接口
        paramGroup.setHasKeepSections(true);//生成的类可以添加自主代码
    }

    /** 参数组 */
    private static void addParam(Schema schema)
    {
        param = schema.addEntity("Param");
        param.setDbName("Param");
        /** 十六进制编号（例：0000） */
        param.addStringProperty("hexNo").dbName("hexNo").primaryKey();
        /** 参数组编号 */
        param.addStringProperty("paramGroupHexNo").dbName("paramGroupHexNo");
        /** 数据类型 */
        param.addStringProperty("type").dbName("type");
        /** 名称 */
        param.addStringProperty("name").dbName("name");
        /** 值 */
        param.addStringProperty("value").dbName("value");
        /** 显示给客户看的值 */
        param.addStringProperty("valueDisplay").dbName("valueDisplay");
        /** 关联变量 */
        param.addStringProperty("linkVariable").dbName("linkVariable");
        /** 选项数量/小数位数 */
        param.addStringProperty("count").dbName("count");
        /** 选项字符串索引/单位 */
        param.addStringProperty("unit").dbName("unit");
        /** 最大值 */
        param.addStringProperty("max").dbName("max");
        /** 最小值 */
        param.addStringProperty("min").dbName("min");
        param.implementsSerializable();//实现序列化接口
        param.setHasKeepSections(true);//生成的类可以添加自主代码
    }
}
