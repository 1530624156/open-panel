package com.mavis.mypanel.logic;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mavis.mypanel.entity.TSystemAttachment;
import com.mavis.mypanel.entity.vo.JsonReturn;
import com.mavis.mypanel.service.TSystemAttachmentService;
import com.mavis.mypanel.util.MyIoUtil;
import com.mavis.mypanel.util.StaticUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.UUID;

@Component
public class AttachmentLogic {

    @Resource
    private TSystemAttachmentService attachmentService;


    /**
     * 上传文件
     *
     * @param file
     * @return
     */
    public JsonReturn upload(MultipartFile file) {
        String md5 = null;
        try {
            //重复文件判断
            InputStream is = file.getInputStream();
            md5 = DigestUtil.md5Hex(is);
        } catch (IOException e) {
            JsonReturn.errorMsg(e.getMessage());
        }
        TSystemAttachment attachment_exist = attachmentService.lambdaQuery().eq(TSystemAttachment::getMd5, md5).one();
        if(attachment_exist != null){
            return JsonReturn.success(attachment_exist);
        }
        //不存在，则上传
        TSystemAttachment attachment = new TSystemAttachment();
        String fname = file.getOriginalFilename();
        long size = file.getSize();
        attachment.setFilename(fname);
        attachment.setSize(size);
        String savePath = StaticUtil.mypanel_save_path;
        File f = new File(savePath);
        if(!f.exists()){
            f.mkdirs();
        }
        String uuid = UUID.randomUUID().toString();
        String suffix = fname.substring(fname.lastIndexOf("."));
        String saveName = uuid + suffix;
        attachment.setUuid(uuid);
        attachment.setSuffix(suffix);
        attachment.setMd5(md5);
        File save = new File(f.getAbsolutePath(), saveName);
        try {
            file.transferTo(save);
        } catch (IOException e) {
            e.printStackTrace();
            return JsonReturn.errorMsg(e.getMessage());
        }
        attachment.setCreateBy(StpUtil.getLoginIdAsLong());
        attachmentService.save(attachment);
        return JsonReturn.success(attachment);
    }

    public void downloadByUuid(String uuid, HttpServletResponse response) {
        QueryWrapper<TSystemAttachment> qw = new QueryWrapper<>();
        qw.eq("uuid", uuid);
        TSystemAttachment attachment = attachmentService.getOne(qw);
        String fileName = attachment.getFilename();
        String filePath = attachment.getUuid() + attachment.getSuffix();
        File file = new File(StaticUtil.mypanel_save_path, filePath);

        response.reset();
        try {
            fileName = URLEncoder.encode(fileName,"UTF-8");
        } catch (UnsupportedEncodingException e) {
        }
        response.addHeader("Content-Length", "" + attachment.getSize());
        response.addHeader("Content-Disposition", "attachment;filename=" + fileName);
        response.addHeader("filename", fileName);
        response.setContentType("application/octet-stream");
        OutputStream outputStream = null;
        try {
            outputStream = new BufferedOutputStream(response.getOutputStream());
            outputStream.write(MyIoUtil.readInputStream(new FileInputStream(file)));
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            System.err.println(String.format("【附件】请求文件异常 %s", URLDecoder.decode(fileName)));
        }
    }

    public TSystemAttachment getByUuid(String tlsCaAttachUuid) {
        return this.attachmentService.getByUuid(tlsCaAttachUuid);
    }
}
