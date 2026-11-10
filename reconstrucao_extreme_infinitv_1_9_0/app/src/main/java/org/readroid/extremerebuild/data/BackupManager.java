package org.readroid.extremerebuild.data;

import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/** Portable, passphrase-encrypted backup format. App-local Keystore ciphertext is never exported. */
public final class BackupManager {
    private static final byte[] MAGIC="READROID-STREAM-BACKUP-1\n".getBytes(StandardCharsets.US_ASCII);
    private static final int SALT_LENGTH=16,IV_LENGTH=12,ITERATIONS=210000,MAX_BYTES=64*1024*1024;
    private BackupManager(){}

    public static void write(OutputStream output,LocalStore store,char[] password)throws Exception{
        requirePassword(password);byte[] salt=new byte[SALT_LENGTH],iv=new byte[IV_LENGTH];SecureRandom random=new SecureRandom();random.nextBytes(salt);random.nextBytes(iv);
        Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.ENCRYPT_MODE,derive(password,salt),new GCMParameterSpec(128,iv));cipher.updateAAD(MAGIC);
        byte[] clear=store.exportSnapshot().toString().getBytes(StandardCharsets.UTF_8);if(clear.length>MAX_BYTES)throw new IllegalArgumentException("Backup excede 64 MiB.");byte[] encrypted=cipher.doFinal(clear);
        output.write(MAGIC);output.write(salt);output.write(iv);output.write(ByteBuffer.allocate(4).putInt(ITERATIONS).array());output.write(encrypted);output.flush();
    }
    public static void restore(InputStream input,LocalStore store,char[] password)throws Exception{
        requirePassword(password);byte[] all=readBounded(input);int headerLength=MAGIC.length+SALT_LENGTH+IV_LENGTH+4;
        if(all.length<headerLength+16||all.length>MAX_BYTES)throw new IllegalArgumentException("Arquivo de backup inválido ou grande demais.");
        for(int i=0;i<MAGIC.length;i++)if(all[i]!=MAGIC[i])throw new IllegalArgumentException("Formato de backup não reconhecido.");
        ByteBuffer data=ByteBuffer.wrap(all);data.position(MAGIC.length);byte[] salt=new byte[SALT_LENGTH],iv=new byte[IV_LENGTH];data.get(salt);data.get(iv);int iterations=data.getInt();if(iterations<100000||iterations>1000000)throw new IllegalArgumentException("Parâmetros de criptografia inválidos.");
        byte[] encrypted=new byte[data.remaining()];data.get(encrypted);Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.DECRYPT_MODE,derive(password,salt),new GCMParameterSpec(128,iv));cipher.updateAAD(MAGIC);
        JSONObject snapshot=new JSONObject(new String(cipher.doFinal(encrypted),StandardCharsets.UTF_8));if(snapshot.optInt("version",0)!=1)throw new IllegalArgumentException("Versão de backup incompatível.");store.restoreSnapshot(snapshot);
    }
    private static SecretKeySpec derive(char[] password,byte[] salt)throws Exception{KeySpec spec=new PBEKeySpec(password,salt,ITERATIONS,256);try{return new SecretKeySpec(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded(),"AES");}finally{((PBEKeySpec)spec).clearPassword();}}
    private static byte[] readBounded(InputStream input)throws Exception{ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] chunk=new byte[8192];int n,total=0;while((n=input.read(chunk))!=-1){total+=n;if(total>MAX_BYTES)throw new IllegalArgumentException("Backup excede 64 MiB.");out.write(chunk,0,n);}return out.toByteArray();}
    private static void requirePassword(char[] password){if(password==null||password.length<8)throw new IllegalArgumentException("Use uma frase secreta com pelo menos 8 caracteres.");}
}
