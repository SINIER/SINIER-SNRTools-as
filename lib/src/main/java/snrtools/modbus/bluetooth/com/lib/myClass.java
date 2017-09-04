package snrtools.modbus.bluetooth.com.lib;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

public class myClass {

//    "" varHexNo==========0000
//             result==========01430000000249f864d1
//             var=====================hexNo===0000,type===1,count===0,unit===000e
//             varHexNo==========0001
//            result==========014300010004dd8745419d14
//var=====================hexNo===0001,type===7,count===3,unit===000e
//             varHexNo==========0002
//           result==========014300020004c7d802046b1d  67295431
// var=====================hexNo===0002,type===6,count===0,unit===0010
//  varHexNo==========0003
//             result==========014300030004a01aaf3e39d6
// var=====================hexNo===0003,type===7,count===3,unit===0010
//          varHexNo==========0004
//    result==========014300040004dd874541c814
// var=====================hexNo===0004,type===7,count===3,unit===000e
//         varHexNo==========0005
//      result==========0143000500021711513f
//        var=====================hexNo===0005,type===4,count===2,unit===0025
//       varHexNo==========0006
//      result==========01430006000205011853
//         var=====================hexNo===0006,type===3,count===1,unit===0026
//            09-03 10:07:21.908 15746-15746/snrtools.modbus.bluetooth.com.snrtools I/System.out: varHexNo==========0007
//            09-03 10:07:21.909 15746-15746/snrtools.modbus.bluetooth.com.snrtools I/System.out: result==========0143000700040000000083f1
//            09-03 10:07:21.909 15746-15746/snrtools.modbus.bluetooth.com.snrtools I/System.out: var=====================hexNo===0007,type===6,count===0,unit===0000
//            09-03 10:07:22.441 15746-15746/snrtools.modbus.bluetooth.com.snrtools I/System.out: varHexNo==========0008
//            09-03 10:07:22.442 15746-15746/snrtools.modbus.bluetooth.com.snrtools I/System.out: result==========014300080004000000007cf1
//09-03 10:07:22.442 15746-15746/snrtools.modbus.bluetooth.com.snrtools I/System.out: var=====================hexNo===0008,type===6,count===0,unit===0000
    public static void main(String[] args)
    {
        //54 e4 fc 20
        long time = NumberBytes.hexStrToLong("20fce454");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            long l2000 = sdf.parse("2000-01-01 00:00:00").getTime();
            System.out.println(sdf.format(new Date(time*1000+l2000)));

        }catch (Exception e){

        }
        System.out.println(time*1000);
        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:MM:ss").format(new Date(time*1000)));


        byte[] c = CRC16.HexString2Buf("42617365000000000000000000000000");
//        byte[] c = CRC16.HexString2Buf("bbf9b1beb2cecafd0000000000000000");
        System.out.println("====3======"+Byte2Char(c));
//0x01 0x61 0x00 0x00 0x00 0x00
        byte[] cmd = CRC16.getSendBuf("016100000000");

    }


    public static String Byte2Char(byte[] b) {
        /**
         *  通过ByteToCharConverter类转换不可行，sun.io.*包属于内部API，已经不可用
         *  ByteToCharConverter converter = ByteToCharConverter.getConverter("gb2312");
         *  char c[] = converter.convertAll(b);
         */

        Charset charSet = Charset.forName("GB2312");
        ByteBuffer byteBuffer = ByteBuffer.allocate(b.length);
        byteBuffer.put(b);
        byteBuffer.flip();
        CharBuffer charBuffer = charSet.decode(byteBuffer);
        return charBuffer.toString();
    }

}
