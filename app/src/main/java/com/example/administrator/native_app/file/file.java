package com.example.administrator.native_app.file;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import com.example.administrator.native_app.R;
import com.example.administrator.native_app.callback.callback;
import com.example.administrator.native_app.config.config;
import com.example.administrator.native_app.data.run_data;
import com.example.administrator.native_app.network.network;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Handler;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sun.misc.BASE64Encoder;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

import org.apache.http.util.EncodingUtils;

import javax.security.auth.callback.Callback;

/**
 * Created by Administrator on 2019/3/4 0004.
 */

public class file {
    public static String bitmapToBase64(Bitmap bitmap) {

        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * base64转为bitmap
     *
     * @param base64Data
     * @return
     */
    public static Bitmap base64ToBitmap(String base64Data) {
        byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
    public static Bitmap jpeg_bitmap_compress(String path,Boolean is_compress,double multiple,final String work_path)throws Exception{
        String type=get_file_type(path);
        int quality=80;
        if(is_compress)
        {
            quality=(int)multiple;
        }
        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inSampleSize =0; // 这个数字越大,图片就越小.图片就越不清晰
        Bitmap pic = null;
        pic = BitmapFactory.decodeFile(path, op);  //先从本地读照片，然后利用op参数对图片进行处理

//将处理后的图片重新写回本地
        FileOutputStream b = null;
        try {
            b = new FileOutputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (pic != null) {
            pic.compress(Bitmap.CompressFormat.JPEG,quality, b);
        }
// 设置图片的大小
        Bitmap bitMap = BitmapFactory.decodeFile(path);
        int width = bitMap.getWidth();
        int height = bitMap.getHeight();
// 设置想要的大小
// 计算缩放比例
        float scaleWidth = 1;
        float scaleHeight =1;
// 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        matrix.setRotate(90, (float)width, (float)height);
// 得到新的图片
        bitMap = Bitmap.createBitmap(bitMap, 0, 0, width, height,matrix, true);

//将新文件回写到本地
        if(true) {
            try {
                b = new FileOutputStream(work_path+ String.format("cache.%s",type));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if (bitMap != null) {
                bitMap.compress(Bitmap.CompressFormat.JPEG, quality, b);
            }
        }
        return bitMap;
    }
    public static String jpeg_base64(String path,Boolean is_delete){
        String base64= String.format("data:image/%s;base64,",get_file_type(path));
        byte[] data=null;
        try {
            data=readFileSdcardFile_Bytes(path);
            BASE64Encoder encoder = new BASE64Encoder();
            base64+=encoder.encode(data);
//            if(false)
//            {
//                File file=new File(path);
//                if(file.isFile())
//                {
//                    file.delete();
//                }
//            }
        }
        catch (Exception E) {
            //对字节数组Base64编码
        }
        finally {
            return base64;
        }
    }
    private static HashMap<Integer, String> serviceTypes = new HashMap();

    public static String getServiceType(int type) {
        return serviceTypes.get(type);
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public static String bytesToString(byte[] src) {
        // byte[] 转 string
        String res = new String(src);
        return res;
    }

    // HexString锟斤拷锟斤拷>byte
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));

        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    // 鍒ゆ柇鏄惁16杩涘埗瀛楃
    public static boolean isHexChar(String str) {
        for (int i = 0; i < str.length(); i++) {
            if ((str.charAt(i) >= '0' && str.charAt(i) <= '9')
                    || (str.charAt(i) >= 'a' && str.charAt(i) <= 'f')
                    || (str.charAt(i) >= 'A' && str.charAt(i) <= 'F')) {
            } else {
                return false;
            }
        }
        return true;
    }

    public static int byteArrayToInt(byte[] b, int offset) {
        int value= 0;
        for (int i = 0; i < 4; i++) {
            int shift= (4 - 1 - i) * 8;
            value +=(b[i + offset] & 0x000000FF) << shift;
        }
        return value;
    }

    /**
     * 字符串转换成十六进制字符串
     * @return String 每个Byte之间空格分隔，如: [61 6C 6B]
     */
    public static String str2HexStr(String str)
    {

        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;

        for (int i = 0; i < bs.length; i++)
        {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    /**
     * 十六进制转换字符串
     * @param String str Byte字符串(Byte之间无分隔符 如:[616C6B])
     * @return String 对应的字符串
     */
    public static String hexStr2Str(String hexStr)
    {
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        int n;

        for (int i = 0; i < bytes.length; i++)
        {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);
        }
        return new String(bytes);
    }

    /**
     * bytes转换成十六进制字符串
     * @param byte[] b byte数组
     * @return String 每个Byte值之间空格分隔
     */
    public static String byte2HexStr(byte[] b)
    {
        String stmp="";
        StringBuilder sb = new StringBuilder("");
        for (int n=0;n<b.length;n++)
        {
            stmp = Integer.toHexString(b[n] & 0xFF);
            sb.append((stmp.length()==1)? "0"+stmp : stmp);
            sb.append(" ");
        }
        return sb.toString().toUpperCase().trim();
    }

    /**
     * bytes字符串转换为Byte值
     * @param String src Byte字符串，每个Byte之间没有分隔符
     * @return byte[]
     */
    public static byte[] hexStr2Bytes(String src)
    {
        int m=0,n=0;
        int l=src.length()/2;
        System.out.println(l);
        byte[] ret = new byte[l];
        for (int i = 0; i < l; i++)
        {
            m=i*2+1;
            n=m+1;
            ret[i] = Byte.decode("0x" + src.substring(i*2, m) + src.substring(m,n));
        }
        return ret;
    }

    /**
     * String的字符串转换成unicode的String
     * @param String strText 全角字符串
     * @return String 每个unicode之间无分隔符
     * @throws Exception
     */
    public static String strToUnicode(String strText)
            throws Exception
    {
        char c;
        StringBuilder str = new StringBuilder();
        int intAsc;
        String strHex;
        for (int i = 0; i < strText.length(); i++)
        {
            c = strText.charAt(i);
            intAsc = (int) c;
            strHex = Integer.toHexString(intAsc);
            if (intAsc > 128)
                str.append("\\u" + strHex);
            else // 低位在前面补00
                str.append("\\u00" + strHex);
        }
        return str.toString();
    }

    /**
     */
    public static String unicodeToString(String hex)
    {
        int t = hex.length() / 6;
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < t; i++)
        {
            String s = hex.substring(i * 6, (i + 1) * 6);
            // 高位需要补上00再转
            String s1 = s.substring(2, 4) + "00";
            // 低位直接转
            String s2 = s.substring(4);
            // 将16进制的string转为int
            int n = Integer.valueOf(s1, 16) + Integer.valueOf(s2, 16);
            // 将int转换为字符
            char[] chars = Character.toChars(n);
            str.append(new String(chars));
        }
        return str.toString();
    }

    public static int _1_Byte_To_Unsigned_Int(Byte b)
    {
        byte lo = (byte)b;
        int ilo = lo;
        ilo = lo&0xff;
        return ilo;
    }


    //写数据到SD中的文件
    public static void writeFileSdcardFile(String fileName,String write_str) throws IOException{
        try{

            FileOutputStream fout = new FileOutputStream(fileName);
            byte [] bytes = write_str.getBytes();

            fout.write(bytes);
            fout.close();
        }

        catch(Exception e){
            e.printStackTrace();
        }
    }
    //写数据到SD中的文件
    public static void writeFileSdcardFile_Bytes(String fileName,byte[] rTotalBuffer, int write_byte_lenght) throws IOException{
        try{

            FileOutputStream fout = new FileOutputStream(fileName);
            //byte [] bytes = write_str.getBytes();
            byte [] bytes = new byte[write_byte_lenght]; //write_str.getBytes();
            System.arraycopy(rTotalBuffer, 0, bytes, 0, write_byte_lenght);

            fout.write(bytes);
            fout.close();
        }

        catch(Exception e){
            e.printStackTrace();
        }
    }
    public static Bitmap file_to_bitmap(String file_path) throws Exception{
        if(!is_file(file_path)){
            throw new Exception("file_not_exist"+file_path);
        }else {
            byte[] data=readFileSdcardFile_Bytes(file_path);
            Bitmap bitmap= BitmapFactory.decodeByteArray(data,0,data.length);
            return bitmap;
        }
    }

    /**
     * @description 追加文件
     * @param fileName
     * @param info
     */
    public static void appendInfoToFile(String fileName, String info) {
        File file =new File(fileName);
        try {
            if(!file.exists()){
                file.createNewFile();
            }
            FileWriter fileWriter =new FileWriter(file, true);
            info =info +System.getProperty("line.separator");
            fileWriter.write(info);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static ArrayList json_list(String data){
        Gson gson=new Gson();
        ArrayList list=new ArrayList();
        Pattern r = Pattern.compile("\\{[\\s\\S]*?\\}");
        Matcher m = r.matcher(data);
        int i=0;
        Log.i("source",data);
        while (m.find())
        {
            HashMap map=gson.fromJson(m.group(),HashMap.class);
            list.add(map);
        }
        return list;
    }
    public static Bitmap get_cycle (Bitmap bitmap) {
// 图片的宽度
        int width = bitmap.getWidth();
// 图片的高度
        int height = bitmap.getHeight();


        int r = width > height ? height : width;


// 创建一个画布的背景
        Bitmap backBitmap = Bitmap
                .createBitmap(width, height, Bitmap.Config.ARGB_8888);
//新建一个画布
        Canvas canvas=new Canvas(backBitmap);
//创建画笔
        Paint paint=new Paint();
        paint.setAntiAlias(true);

        RectF rectF=new RectF(0, 0, r, r);
//先画圆
        canvas.drawRoundRect(rectF, r/2, r/2, paint);

//设置两幅图相交时的一个画笔的处理模式
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

//画头像
        canvas.drawBitmap(bitmap, null, rectF, paint);

        return backBitmap;
    }

    public static String readFileSdcardFile(String fileName) throws IOException{
        String res="";
        try{
            FileInputStream fin = new FileInputStream(fileName);

            int length = fin.available();
            int file_len = length;
            byte [] buffer = new byte[length];
            fin.read(buffer);

            res = EncodingUtils.getString(buffer, "UTF-8");

            fin.close();
        }

        catch(Exception e){
            e.printStackTrace();
        }
        return res;
    }

    public static byte[] readFileSdcardFile_Bytes(String fileName) throws IOException{
        try{
            FileInputStream fin = new FileInputStream(fileName);

            int length = fin.available();
            int file_len = length;

            byte [] buffer = new byte[length];
            fin.read(buffer);

            //res = EncodingUtils.getString(buffer, "UTF-8");

            fin.close();

            return buffer;
        }

        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public static byte[] file_byte(File file){
        try{
            byte[] buffer = null;
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1)
            {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
            return buffer;
        }
        catch(Exception e){
            Log.i("byte_exception",e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    public static void log_data(String data1,String data2)
    {
        try {
            SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date d=new Date();
            String date = sDateFormat.format(d);
            String file_name = "sdcard/img_video/log/"+"log-" + date + ".txt";
            File f = new File(file_name);
            if (!f.exists()) {
                f.createNewFile();
            }
            Calendar c = Calendar.getInstance();//
            FileWriter f_w=new FileWriter(file_name,true);
            String DATA=data1+" "+data2+" "+c.get(Calendar.HOUR_OF_DAY)+"-"+c.get(Calendar.MINUTE);
            f_w.write(DATA+"\n");
            f_w.flush();
        }
        catch (Exception E){
            E.getStackTrace();
        }
    }
    public static void  file_path_create(String path){
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
    }
    public static String ByteToString(byte[] bytes)
    {
        StringBuilder strBuilder = new StringBuilder();
        for (int i = 0; i <bytes.length ; i++) {
            if (bytes[i]!=0){
                strBuilder.append((char)bytes[i]);
            }else {
                break;
            }

        }
        return strBuilder.toString();
    }
    public static String[] get_log_list(String file_path,String extend){
        File F=new File(file_path);
        File file[]=F.listFiles();
        String[] file_list;
        String[] truth_list;
        file_list=new String[file.length];
        int index=0;
        for(int i=0;i<file.length;i++)
        {
            String name=file[i].getName();
            String ex=name.substring(name.lastIndexOf('.')+1);
            if(ex.equals(extend)) {
                file_list[index++] =file[i].getName();
            }
        }
        truth_list=new String[index];
        for(int i=0;i<index;i++)
        {
            truth_list[i]=file_list[i];
        }
        return truth_list;
    }
    public static String bytes2kb(long bytes) {
        BigDecimal filesize = new BigDecimal(bytes);
        BigDecimal megabyte = new BigDecimal(1024 * 1024);
        float returnValue = filesize.divide(megabyte, 2, BigDecimal.ROUND_UP)
                .floatValue();
        if (returnValue > 1)
            return (returnValue + "MB");
        BigDecimal kilobyte = new BigDecimal(1024);
        returnValue = filesize.divide(kilobyte, 2, BigDecimal.ROUND_UP)
                .floatValue();
        return (returnValue + "KB");
    }
    public static int multiple(double lenth){
        return 1;
    }
    public static String get_file_type(String path){
        String[] name_list=path.split("\\.");
        return name_list[name_list.length-1];
    }
    public static String get_file_name(String path){
        String[] name_list=path.split("/");
        return name_list[name_list.length-1];
    }
    public static Boolean is_file(String path){
        File file = new File(path);
        if (!file.exists()) {
            return false;
        }else {
           return true;
        }
    }
    public static void file_compress(String path, Context context, final android.os.Handler handler, final callback callback){
        File file_=new File(path);
        Luban.with(context).load(file_).setCompressListener(new OnCompressListener() {
            @Override
            public void onStart() {

            }
            @Override
            public void onSuccess(File file_) {
                byte[] buffer=file.file_byte(file_);
                try {
                    callback.seccess(buffer,"");
                }
                catch (Exception e){
                    callback.fail(e.getMessage(),"");
                }
            }
            @Override
            public void onError(Throwable e) {
                callback.fail(e.getMessage(),"");
            }
        }).launch();
    }
    public static void load_network_file(String url, android.os.Handler handler, final ImageView imageView){
        try {
            Log.i("file_name",file.get_file_name(url));
            final String cache_path = config.work_path() + "cache/" + file.get_file_name(url);
            if (file.is_file(cache_path)) {
                imageView.setImageBitmap(file_to_bitmap(cache_path));
                return;
            }
            final String key=url+"image_list";
            if(run_data.singleton().get(key)==null){
                ArrayList image_list=new ArrayList();
                image_list.add(imageView);
                run_data.singleton().set(key,image_list);
            }
            else {
                ArrayList image_list=(ArrayList) run_data.singleton().get(key);
                image_list.add(imageView);
                run_data.singleton().set(key,image_list);
            }
            network.singleton(handler).get(url, new HashMap<String, String>(), new callback() {
                @Override
                public void fail(String message, String url) {
                    imageView.setImageResource(R.drawable.fail_load);
                }

                @Override
                public void seccess(byte[] data, String url) {
                    Log.i("data_url", url);
                    try {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        ArrayList image_list=(ArrayList) run_data.singleton().get(key);
                        for (int i=0;i<image_list.size();i++){
                            ImageView imageView1=(ImageView) image_list.get(i);
                            imageView1.setImageBitmap(bitmap);
                        }
                        file.writeFileSdcardFile_Bytes(cache_path, data, data.length);
                    } catch (Exception E) {

                    }
                    finally {
                        run_data.singleton().delete(key);
                    }
                }
            });
        }
        catch (Exception e){
            imageView.setImageResource(R.drawable.fail_load);
        }
//                player.setText(room_info.get("user_name")+"");
    }
    public static void load_network_file_no_cache(String url, android.os.Handler handler, final ImageView imageView){
        try {
            network.singleton(handler).get(url, new HashMap<String, String>(), new callback() {
                @Override
                public void fail(String message, String url) {
                    imageView.setImageResource(R.drawable.fail_load);
                }

                @Override
                public void seccess(byte[] data, String url) {
                    Log.i("data_url", url);
                    try {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        imageView.setImageBitmap(bitmap);
                    } catch (Exception E) {
                        imageView.setImageResource(R.drawable.fail_load);
                    }
                }
            });
        }
        catch (Exception e){
            imageView.setImageResource(R.drawable.fail_load);
        }
//                player.setText(room_info.get("user_name")+"");
    }
}
