package com.minio.admin;

import com.minio.minio_test.admin.GroupInfo;
import com.minio.minio_test.admin.MinioAdminClient;
import com.minio.minio_test.admin.Status;
import com.minio.minio_test.admin.UserInfo;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

class MinioTestApplicationTests {

    /**
     * 查询所有用户信息
     */
    @Test
    void minioListUsers() throws InvalidCipherTextException, NoSuchAlgorithmException, IOException, InvalidKeyException {
        MinioAdminClient.Builder minioAdminClient = new MinioAdminClient.Builder();
        minioAdminClient.endpoint("http://124.70.53.210:9080");
        minioAdminClient.credentials("minio_user", "528528528");
        Map<String, UserInfo> stringUserInfoMap = minioAdminClient.build().listUsers();
        Set<String> keySet = stringUserInfoMap.keySet();
        for (String str : keySet) {
            System.out.println("用户名：" + str + "\t" + "密码为：" + stringUserInfoMap.get(str).secretKey() + "\t" + "权限名称：" + stringUserInfoMap.get(str).policyName() + "\t" + "所属群组为：" + stringUserInfoMap.get(str).memberOf());
        }
    }

    /**
     * 新增用户
     */
    @Test
    void addUsers() throws InvalidCipherTextException, NoSuchAlgorithmException, IOException, InvalidKeyException {
        MinioAdminClient.Builder minioAdminClient = new MinioAdminClient.Builder();
        minioAdminClient.endpoint("http://124.70.53.210:9080");
        minioAdminClient.credentials("minio_user", "528528528");
        List<String> list = new ArrayList<>();
        list.add("minio_test");
        minioAdminClient.build().addUser("minio2_demo", UserInfo.Status.ENABLED, "528528528", "readonly", list);
    }

    @Test
    void getUserInfo() throws NoSuchAlgorithmException, IOException, InvalidKeyException {
        MinioAdminClient.Builder minioAdminClient = new MinioAdminClient.Builder();
        minioAdminClient.endpoint("http://124.70.53.210:9080");
        minioAdminClient.credentials("minio_user", "528528528");
        UserInfo minioUser = minioAdminClient.build().getUserInfo("minio_user");
        System.out.println("用户群组为：" + minioUser.memberOf() + "\t用户政策为：" + minioUser.policyName() + "\t用户的状态为：" + minioUser.status() + "\t用户的密码为：" + minioUser.secretKey());
    }

    /**
     * 删除用户
     */
    @Test
    void deleteUsers() throws NoSuchAlgorithmException, IOException, InvalidKeyException {
        MinioAdminClient.Builder minioAdminClient = new MinioAdminClient.Builder();
        minioAdminClient.endpoint("http://124.70.53.210:9080");
        minioAdminClient.credentials("minio_user", "528528528");
        minioAdminClient.build().deleteUser("minio2_demo");
    }

    /**
     * 新增或修改群组信息
     */
    @Test
    void addUpdateGroup() throws NoSuchAlgorithmException, IOException, InvalidKeyException {
        MinioAdminClient.Builder minioAdminClient = new MinioAdminClient.Builder();
        minioAdminClient.endpoint("http://124.70.53.210:9080");
        minioAdminClient.credentials("minio_user", "528528528");
        ArrayList<String> list = new ArrayList<>();
        list.add("minio2_demo");
        minioAdminClient.build().addUpdateGroup("minio_group_1", Status.ENABLED, list);
    }

    /**
     * 获取所有群组信息
     */
    @Test
    void listGroups() throws NoSuchAlgorithmException, IOException, InvalidKeyException {
        MinioAdminClient.Builder minioAdminClient = new MinioAdminClient.Builder();
        minioAdminClient.endpoint("http://124.70.53.210:9080");
        minioAdminClient.credentials("minio_user", "528528528");
        List<String> listGroups = minioAdminClient.build().listGroups();
        System.out.println("所有群组的信息：" + listGroups);
    }

    /**
     * 设置用户和群组的政策
     */
    @Test
    void setPolicy() throws NoSuchAlgorithmException, IOException, InvalidKeyException {
        MinioAdminClient.Builder minioAdminClient = new MinioAdminClient.Builder();
        minioAdminClient.endpoint("http://124.70.53.210:9080");
        minioAdminClient.credentials("minio_user", "528528528");
        minioAdminClient.build().setPolicy("minio2_demo", false, "readwrite");
    }

    /**
     * 获取群组相关信息
     */
    @Test
    void getGroupInfo() throws NoSuchAlgorithmException, IOException, InvalidKeyException {
        MinioAdminClient.Builder minioAdminClient = new MinioAdminClient.Builder();
        minioAdminClient.endpoint("http://124.70.53.210:9080");
        minioAdminClient.credentials("minio_user", "528528528");
        GroupInfo minio_test = minioAdminClient.build().getGroupInfo("minio_test");
        System.out.println("群组的用户有：" + minio_test.members());
    }


}
