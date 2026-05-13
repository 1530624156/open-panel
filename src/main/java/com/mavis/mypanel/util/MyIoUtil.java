package com.mavis.mypanel.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * IO工具
 *
 * @author 杨立伦
 */
public class MyIoUtil {

    /**
     * 读取输入流成字符串
     *
     * @param is
     * @return
     */
    public static String readInputStreamAsString(InputStream is) {
        return readInputStreamAsString(is, "utf-8");
    }

    /**
     * 读取输入流成字符串
     *
     * @param is
     * @param charset
     * @return
     */
    public static String readInputStreamAsString(InputStream is, String charset) {
        if (charset == null) {
            charset = "utf-8";
        }
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(is, charset));
        } catch (UnsupportedEncodingException e) {
            return null;
        }
        String temp = "";
        StringBuffer sb = new StringBuffer();

        try {
            while ((temp = br.readLine()) != null) {
                sb.append(temp + "\n");
            }
            br.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
        return sb.toString();
    }


    /**
     * 获取目录下所有文件名称
     *
     * @param dirpath 路径
     * @return 文件名称列表
     */
    public static List<String> getAllFileName(String dirpath) {
        File dir = new File(dirpath);
//        如果文件不存在
        if (!dir.exists()) {
            return null;
        }

        if (!dir.isFile()) {
            File[] files = dir.listFiles();
            ArrayList<String> fnames = new ArrayList<>();
            for (File temp : files) {
                fnames.add(temp.getName());
            }
            return fnames;
        } else {
            return null;
        }
    }

    /**
     * byte写到文件中
     *
     * @param bytes
     * @param fname
     */
    public static void writeByteToFile(byte[] bytes, String fname) {
        File file = new File(fname);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        try {
            FileOutputStream fos = new FileOutputStream(fname);
            fos.write(bytes);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * io写到文件中
     *
     * @param inputStream
     * @param fname
     */
    public static void writeStreamToFileWithClose(InputStream inputStream, String fname) {
        File file = new File(fname);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(fname);
            byte[] bytes = new byte[1024];
            int len;
            while((len=inputStream.read(bytes))!=-1){
                fileOutputStream.write(bytes,0,len);
            }
            fileOutputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeUrlToFile(String downloadurl,String fname) throws IOException {
        URL url = new URL(downloadurl);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestProperty("Referer","https://i.chaoxing.com/");

        System.out.println("响应状态码:"+urlConnection.getResponseCode());
        File savepath = new File(fname);

        if(urlConnection.getResponseCode() == 200){
            System.out.println("开始下载~");
            InputStream inputStream = urlConnection.getInputStream();
            FileOutputStream fileOutputStream = new FileOutputStream(savepath);
            byte[] bytes = new byte[1024];
            int len;
            while((len=inputStream.read(bytes))!=-1){
                fileOutputStream.write(bytes,0,len);
            }
            fileOutputStream.close();
            inputStream.close();
            urlConnection.disconnect();
        }
        System.out.println("保存成功："+savepath.getAbsolutePath());
    }

    /**
     * 输入流转字节数组
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static byte[] readInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        //创建一个Buffer字符串
        byte[] buffer = new byte[1024];
        //每次读取的字符串长度，如果为-1，代表全部读取完毕
        int len = 0;
        //使用一个输入流从buffer里把数据读取出来
        while ((len = inputStream.read(buffer)) != -1) {
            //用输出流往buffer里写入数据，中间参数代表从哪个位置开始读，len代表读取的长度
            outStream.write(buffer, 0, len);
        }
        //把outStream里的数据写入内存
        return outStream.toByteArray();
    }

    /**
     * 向文件追加一行
     *
     * @param filepath
     * @param line
     */
    public static void appendLineToFile(String filepath, String line) {
        File f = new File(filepath);
        if (!f.exists()) {
            System.out.println("文件不存在");
            return;
        }
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f, true),"utf-8"));
            bw.write("\n" + line);
            bw.flush();
            bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 向文件追加多行
     *
     * @param filepath
     * @param lines
     */
    public static void appendLinesToFile(String filepath, String lines[]) {
        File f = new File(filepath);
        if (!f.exists()) {
            System.out.println("文件不存在");
            return;
        }
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f, true)));
            for (String line : lines) {
                bw.write("\n" + line);
                bw.flush();
            }
            bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 文件复制方法
     *
     * @param file            要复制的 文件/文件夹
     * @param TargetDirectory 目标文件夹下
     */
    public static void copyto(File file, File TargetDirectory) {
        File TargetFile = new File(TargetDirectory, file.getName());

        if (file.isDirectory()) {

            TargetFile.mkdir();
            File[] files = file.listFiles();
            for (File f : files) {
                copyto(f, TargetFile);
            }
        } else {

            if (TargetDirectory.exists()) {
                File targetFile = new File(TargetDirectory, file.getName());
                if (targetFile.exists() && targetFile.length() == file.length()) {
                    System.out.println(targetFile + " 已存在，跳过");
                    return;
                }
            }
            try (InputStream is = new FileInputStream(file);
                 OutputStream os = new FileOutputStream(TargetFile)) {

                byte[] buf = new byte[1024];
                int length = 0;
                while ((length = is.read(buf)) > 0) {
                    os.write(buf, 0, length);
                }
                //System.out.println("拷贝完成！");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取文件夹下所有文件
     *
     * @param dir
     * @return
     */
    public static ArrayList<File> getAllFileByDir(File dir){
        ArrayList<File> list = new ArrayList<>();
        getAllFileByDirRoleback(dir,list);
        return list;
    }
    private static void getAllFileByDirRoleback(File dir,ArrayList<File> list){
        for (File file : dir.listFiles()) {
            if(file.isDirectory()){
                getAllFileByDirRoleback(file,list);
            }else {
                list.add(file);
            }
        }
    }

    public static void deleteFileOrDir(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                File files[] = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    deleteFileOrDir(files[i]);
                }
            }
        }
    }
}
