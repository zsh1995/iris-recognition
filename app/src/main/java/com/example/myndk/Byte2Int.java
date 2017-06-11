package com.example.myndk;

/**
 * Created by Administrator on 2016/11/23.
 */
public class Byte2Int {
    public static byte[] int2ByteArray(int in){
        byte[] b = new byte[4];

        for(int i = 0;i < 4; i++){
            b[i] = (byte) (in >> 8 * (3 - i) & 0xFF);
        }
        return b;
    }
    public static byte[] intArray2ByteArray(byte[] byte1,byte[] byte2){
        byte[] byte3 = new byte[byte1.length+byte2.length];
        System.arraycopy(byte1,0,byte3,0,byte1.length);
        System.arraycopy(byte2,0,byte3,byte1.length,byte2.length);
        return byte3;
    }

    public static byte[] intArray2ByteArray(int[] input){
        byte[] finalByteArray = int2ByteArray(input[0]);
        for(int i = 1 ; i < input.length;i++){
            byte[] temp = finalByteArray;
            finalByteArray = intArray2ByteArray(temp, int2ByteArray(input[i]));
        }
        return finalByteArray;
    }

}
