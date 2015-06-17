package hamming;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class HammingEncoding {

	static FileInputStream fis;
	static FileInputStream fis2;
	static FileOutputStream fos;
	static double p;
	static Random G;
	static int doubleErrorCounter;
	static int[] buffer;
	static int partOfBuffer;
	
	public static void main(String[] args) throws IOException {
		
		if(args.length > 0){
			
			if(args[0].equals("koder")){
				
				fis = new FileInputStream(args[1]);
				fos = new FileOutputStream(args[2]);
				
				int symbol;
				
				int[] b = new int[8];
				
				while ((symbol = fis.read())!=-1)
				{
					b = to01array(symbol);
					fos.write(toNumber(encode(firstPart(b))));
					fos.write(toNumber(encode(secondPart(b))));
				}
				
				fis.close();
				fos.close();
				
				System.out.println("Kodowanie zakonczone");
				
			} else if(args[0].equals("szum")){
				
				G = new Random();
				
				p = Double.parseDouble(args[1]);
				
				fis = new FileInputStream(args[2]);
				fos = new FileOutputStream(args[3]);
				
				int a[] = new int[8];
				
				int symbol;
				
				while((symbol = fis.read())!=-1)
				{
					a = to01array(symbol);
					flipBits(a);
					fos.write(toNumber(a));
				}
				
				fis.close();
				fos.close();
				
				
				
			} else if(args[0].equals("dekoder")){
				
				fis = new FileInputStream(args[1]);
				fos = new FileOutputStream(args[2]);
				
				buffer = new int[8];
				partOfBuffer = 1;
				
				int a[] = new int[8];
				
				int symbol;
				
				while((symbol = fis.read())!=-1)
				{
					a = to01array(symbol);
					writeToFile(decode(a));
				}
				
				fis.close();
				fos.close();
				
				System.out.println("Ilosc podwójnych błędów:" + doubleErrorCounter);
				
			} else if(args[0].equals("sprawdz")){
				
				fis = new FileInputStream(args[1]);
				fis2 = new FileInputStream(args[2]);
	
				long counter = 0;
				
				int symbol;
				
				int[] a = new int[8];
				int[] b = new int[8];
				
				while( ( symbol = fis.read() )!=-1)
				{
					a = to01array(symbol);
					b = to01array(fis2.read());
					
					if( !areTheSame(firstPart(a),firstPart(b))) counter++;
					if( !areTheSame(secondPart(a),secondPart(b))) counter++;
					
				}
				
				fis.close();
				fis2.close();
				
				System.out.println("Ilość różniących się bloków: " + counter);
				
				
			} else System.out.println("Nieprawidlowe polecenie");
			
		} else System.out.println("Wprowadz polecenie w linii komend");
	

	}
	
	private static int[] to01array(int b)
	{
		int [] result = new int[8];
		
		String res = "";
		
		while (b > 0){
			
			if(b % 2 == 0){
				
				res = "0" + res;
				
			} else {
				
				res ="1" + res;
				
			}
			b /= 2;
			
		}
		
		while(res.length() < 8) res = "0" + res;
		
		for (int i = 0; i < 8; i++)
		{
			if(res.charAt(i)=='1') result[i] = 1;
			else result[i] = 0;
		}
		
		return result;
	}
	
	private static int[] firstPart(int[] tab)
	{
		int[] res = new int[4];
		
		for(int i = 0; i < 4; i++) res[i] = tab[i];
		
		return res;
	}
	
	private static int[] secondPart(int[] tab)
	{
		int[] res = new int[4];
		
		for(int i = 4; i < 8; i++) res[i-4] = tab[i];
		
		return res;
	}
	
	private static int[] encode(int[] info)
	{
		
		int[] res = new int[8];
		
		for(int i = 0; i < 4; i++) res[i] = info[i];
		
		res[4] = (info[1] + info[2]+ info[3]) % 2;
		res[5] = (info[0] + info[2]+ info[3]) % 2;
		res[6] = (info[0] + info[1]+ info[3]) % 2;
		res[7] = (res[0] +res[1] +res[2] +res[3] +res[4] +res[5] +res[6]) % 2;
		
		return res;	
		
	}
	
	private static int toNumber(int[] tab)
	{		
		int res = 0;
		
		for(int i = 0; i < 8; i++){
			res += Math.pow(2, i)*tab[7-i];
		}
		
		return res;
	}
	
	//porównuje bloku złożone z czterech bitów
	private static boolean areTheSame(int [] a, int [] b)
	{
		for (int i = 0; i < 4; i ++)
		{
			if(a[i]!=b[i]) return false;
		}
		return true;
	}
	
	private static void flipBits(int [] a)
	{
		for(int i = 0; i < 8; i++){
			if(G.nextDouble() < p)	a[i] = (a[i]+1)%2;
		}
	}
	
	private static int [] decode(int [] a)
	{
		int res[] = new int[4];
		
		res[0] = a[0];
		res[1] = a[1];
		res[2] = a[2];
		res[3] = a[3];
		
		int wrongBit = ((a[3]+a[4]+a[5]+a[6])%2)*4 + ((a[1]+a[2]+a[5]+a[6])%2)*2 + (a[0]+a[2]+a[4]+a[6])%2;
		int parityCheck = (a[0]+a[1]+a[2]+a[3]+a[4]+a[5]+a[6]+a[7])%2;
		
		boolean parityOk;
		
		if(parityCheck == 0) parityOk = true;
		else parityOk = false;
		
		if(wrongBit > 0){			
			
			if(parityOk) //wystąpiły dwa błędy
			{
				doubleErrorCounter++;	
			} else { //wystapil jeden blad	
				if (wrongBit <= 4) res[wrongBit-1] = (res[wrongBit-1] + 1) %2;	
			}
		}
		
		return res;
	}
	
	public static void writeToFile(int a[]) throws IOException
	{
		if(partOfBuffer == 1){
			
			for(int i = 0; i < 4; i++) buffer[i] = a[i];
			
			partOfBuffer = 2;
		} else {
			
			for(int i = 4; i < 8; i++) buffer[i] = a[i-4];
			
			fos.write(toNumber(buffer));
			
			partOfBuffer = 1;
			
		}
	}

}
