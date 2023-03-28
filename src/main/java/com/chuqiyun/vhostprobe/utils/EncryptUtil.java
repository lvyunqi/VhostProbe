package com.chuqiyun.vhostprobe.utils;

import org.apache.tomcat.util.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * @author mryunqi
 * @date 2023/1/16
 */
public class EncryptUtil {
    public static final String MD5 = "MD5";
    public static final String SHA1 = "SHA1";
    public static final String HMACMD5 = "HmacMD5";
    public static final String HMACSHA1 = "HmacSHA1";
    public static final String DES = "DES";
    public static final String AES = "AES";
    /**
     * md5盐
     */
    private static final String SALT = "Uking.com";
    private static final String Key = "mryunqi";

    /**编码格式；默认使用uft-8*/
    public static String charset = "utf-8";
    /**DES*/
    public static int keysizeDES = 0;
    /**AES*/
    public static int keysizeAES = 128;

    public static EncryptUtil me;

    private EncryptUtil(){
        //单例
    }
    //双重锁
    public static synchronized EncryptUtil getInstance(){
        if (me==null) {
            me = new EncryptUtil();
        }
        return me;
    }

    private static String messageDigest(String res,String algorithm){
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] resBytes = charset==null?res.getBytes():res.getBytes(charset);
            return base64(md.digest(resBytes));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String keyGeneratorMac(String res,String algorithm,String key){
        try {
            SecretKey sk = null;
            if (key==null) {
                KeyGenerator kg = KeyGenerator.getInstance(algorithm);
                sk = kg.generateKey();
            }else {
                byte[] keyBytes = charset==null?key.getBytes():key.getBytes(charset);
                sk = new SecretKeySpec(keyBytes, algorithm);
            }
            Mac mac = Mac.getInstance(algorithm);
            mac.init(sk);
            byte[] result = mac.doFinal(res.getBytes());
            return base64(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String keyGeneratorES(String res,String algorithm,String key,int keysize,boolean isEncode){
        try {
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            KeyGenerator kg = KeyGenerator.getInstance(algorithm);
            if (keysize == 0) {
                byte[] keyBytes = charset==null?key.getBytes():key.getBytes(charset);
                random.setSeed(keyBytes);
                kg.init(random);
            }else if (key==null) {
                kg.init(keysize);
            }else {
                byte[] keyBytes = charset==null?key.getBytes():key.getBytes(charset);
                random.setSeed(keyBytes);
                kg.init(random);
            }
            SecretKey sk = kg.generateKey();
            SecretKeySpec sks = new SecretKeySpec(sk.getEncoded(), algorithm);
            Cipher cipher = Cipher.getInstance(algorithm);
            if (isEncode) {
                cipher.init(Cipher.ENCRYPT_MODE, sks);
                byte[] resBytes = charset==null?res.getBytes():res.getBytes(charset);
                return parseByte2HexStr(cipher.doFinal(resBytes));
            }else {
                cipher.init(Cipher.DECRYPT_MODE, sks);
                return new String(cipher.doFinal(parseHexStr2Byte(res)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String base64(byte[] res){
        return new String(Base64.encodeBase64(res));
    }

    /**将二进制转换成16进制 */
    public static String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }
    /**将16进制转换为二进制*/
    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1) {
            return null;
        }
        byte[] result = new byte[hexStr.length()/2];
        for (int i = 0;i< hexStr.length()/2; i++) {
            int high = Integer.parseInt(hexStr.substring(i*2, i*2+1), 16);
            int low = Integer.parseInt(hexStr.substring(i*2+1, i*2+2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

    /**
     * md5加密算法进行加密（不可逆）
     * @param res 需要加密的原文
     * @return
     */
    public static String md5Method(String res) {
        return messageDigest(res, MD5);
    }

    /**
     * md5加密算法进行加密（不可逆）
     * @param res  需要加密的原文
     * @param key  秘钥
     * @return
     */
    public static String md5Method(String res, String key) {
        return keyGeneratorMac(res, HMACMD5, key);
    }

    /**
     * 使用SHA1加密算法进行加密（不可逆）
     * @param res 需要加密的原文
     * @return
     */
    public static String sha1Method(String res) {
        return messageDigest(res, SHA1);
    }

    /**
     * 使用SHA1加密算法进行加密（不可逆）
     * @param res 需要加密的原文
     * @param key 秘钥
     * @return
     */
    public static String sha1Method(String res, String key) {
        return keyGeneratorMac(res, HMACSHA1, key);
    }

    public static String desEncode(String res, String key) {
        return keyGeneratorES(res, DES, key, keysizeDES, true);
    }

    public static String desDecode(String res, String key) {
        return keyGeneratorES(res, DES, key, keysizeDES, false);
    }

    public String aesEncode(String res, String key) {
        return keyGeneratorES(res, AES, key, keysizeAES, true);
    }

    public String aesDecode(String res, String key) {
        return keyGeneratorES(res, AES, key, keysizeAES, false);
    }

    /**
     * 使用异或进行加密
     * @param res 需要加密的密文
     * @param key 秘钥
     * @return
     */
    public String xorEncode(String res, String key) {
        byte[] bs = res.getBytes();
        for (int i = 0; i < bs.length; i++) {
            bs[i] = (byte) ((bs[i]) ^ key.hashCode());
        }
        return parseByte2HexStr(bs);
    }

    /**
     * 使用异或进行解密
     * @param res 需要解密的密文
     * @param key 秘钥
     * @return
     */
    public String xorDecode(String res, String key) {
        byte[] bs = parseHexStr2Byte(res);
        for (int i = 0; i < bs.length; i++) {
            bs[i] = (byte) ((bs[i]) ^ key.hashCode());
        }
        return new String(bs);
    }

    /**
     * 直接使用异或（第一调用加密，第二次调用解密）
     * @param res 密文
     * @param key 秘钥
     * @return
     */
    public int xor(int res, String key) {
        return res ^ key.hashCode();
    }

    /**
     * 使用Base64进行加密
     * @param res 密文
     * @return
     */
    public static String base64Encode(String res) {
        return new String(Base64.encodeBase64(res.getBytes()));
    }

    /**
     * 使用Base64进行解密
     * @param res
     * @return
     */
    public static String base64Decode(String res) {
        return new String(Base64.decodeBase64(res));
    }

    /**
     * md5算法进行密码加密
     * @param str
     * */
    public static String md5Code(String str){
        try{
            //1.获取MessageDigest对象  生成一个MD5加密计算摘要
            MessageDigest md = MessageDigest.getInstance("MD5") ;
            /*
            str.getBytes()
            * 使用平台的默认字符集将此 String 编码为 byte 序列，并将结果存储到一个新的 byte 数组中.
            此方法多用在字节流中，用与将字符串转换为字节。
            * */

            // 计算md5函数 使用指定的字节数组更新摘要md
            md.update(str.getBytes());
            /*
             * digest()最后确定返回md5 hash值，返回值为8的字符串。
             * 因为md5 hash值是16位的hex值，实际上就是8位的
             * */
            byte[] byteDigest = md.digest() ;
            int i ;
            StringBuilder buf = new StringBuilder() ;
            //遍历byteDigest
            //加密逻辑，可以debug自行了解一下加密逻辑
            for (byte b : byteDigest) {
                i = b;
                if (i < 0) {
                    i += 256;
                }
                if (i < 16) {
                    buf.append("0");
                }
                // BigInteger函数则将8位的字符串转换成16位hex值，用字符串来表示；得到字符串形式的hash值
                buf.append(Integer.toHexString(i));
            }
            return buf.toString() ;
        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
            return null ;
        }
    }

    /**
     * 密码md5加密
     *
     * @param password 密码
     */
    public static String md5(String password) {
        return EncryptUtil.desEncode(password,Key);
    }

    /**
     * 密码比对
     *
     * @param password    未加密的密码
     * @param md5password 加密过的密码
     */
    public static boolean verifyPassword(String password, String md5password) {
        String encryptPasswd = EncryptUtil.desEncode(password,Key);
        return md5(encryptPasswd).equals(md5password);
    }
    /**
     *
     * public static void main(String[] args) throws Exception {
     *         String key = "mimakey";
     *         String encryptUsername = desEncode("root",key);
     *         String encryptPasswd = desEncode("admin",key);
     *         // B57D6F1CC7B27406
     *         System.out.println("encrypt_username:"+encryptUsername);
     *         // 32F8B91B9F68477E5C793D767DD52574
     *         System.out.println("encrypt_passwd:"+encryptPasswd);
     *         String decryptUsername = desDecode(encryptUsername,key);
     *         String decryptPasswd = desDecode(encryptPasswd,key);
     *         System.out.println("解密_username:"+decryptUsername);
     *         System.out.println("解密_passwd:"+decryptPasswd);
     *     }
     */

}