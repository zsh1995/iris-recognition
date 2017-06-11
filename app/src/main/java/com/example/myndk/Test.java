package com.example.myndk;

public class Test {
	
	//定义so库的接口函数
	public native int test(int[] num);
	//预处理
	public native int JniPreProcess(char[] image, int width, int height, int[] Info);
	//特征提取
	public native int JniFeaEx(char[] image, int height, int width, int imageNum, int[] Info, int[] index, char[] Ccodes);
	//特征匹配
	public native int JniMatch(char[] Sample, char[] Templates, int codeNum, int[] best);
	public native int JniAdd( int width, int height);

	
	
	
}
