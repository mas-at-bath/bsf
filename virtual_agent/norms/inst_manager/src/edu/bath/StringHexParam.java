/*
 * 		@author		V Baines
 * 		@date		March 2016
 * 
 */

package edu.bath;

import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.codec.binary.Hex;

public class StringHexParam {

	private ArrayList<String> paramsString= new ArrayList<String>();
	
	public StringHexParam()
	{	
	}

	public void addStringItem(String newItem)
	{
		if (newItem.length() > 30)
		{
			System.out.println("WARNING! Adding quite a long string, not sure what will happen!!");
		}
		paramsString.add(newItem);
	}

	public String getHexParam()
	{
		int numItems = paramsString.size();
		String finalString = "";
		if (numItems == 1)
		{
			String L1 = StringUtils.leftPad(Integer.toHexString(32*1), 64, '0');
			finalString = L1;
		}
		else if (numItems == 2)
		{
			String L1 = StringUtils.leftPad(Integer.toHexString(32*2), 64, '0');
			String L2 = StringUtils.leftPad(Integer.toHexString(32*4), 64, '0');
			finalString = L1+L2;
		}
		else if (numItems == 3)
		{
			String L1 = StringUtils.leftPad(Integer.toHexString(32*3), 64, '0');
			String L2 = StringUtils.leftPad(Integer.toHexString(32*5), 64, '0');	
			String L3 = StringUtils.leftPad(Integer.toHexString(32*7), 64, '0');	
			finalString = L1+L2+L3;
		}
		else if (numItems == 4)
		{
			String L1 = StringUtils.leftPad(Integer.toHexString(32*4), 64, '0');
			String L2 = StringUtils.leftPad(Integer.toHexString(32*6), 64, '0');	
			String L3 = StringUtils.leftPad(Integer.toHexString(32*8), 64, '0');	
			String L4 = StringUtils.leftPad(Integer.toHexString(32*10), 64, '0');	
			finalString = L1+L2+L3+L4;
		}
		else if (numItems == 5)
		{
			String L1 = StringUtils.leftPad(Integer.toHexString(32*5), 64, '0');
			String L2 = StringUtils.leftPad(Integer.toHexString(32*7), 64, '0');	
			String L3 = StringUtils.leftPad(Integer.toHexString(32*9), 64, '0');	
			String L4 = StringUtils.leftPad(Integer.toHexString(32*11), 64, '0');	
			String L5 = StringUtils.leftPad(Integer.toHexString(32*13), 64, '0');	
			finalString = L1+L2+L3+L4+L5;
		}

		for (String st: paramsString)
		{
			try
			{
				String hexString = Hex.encodeHexString(st.getBytes("UTF-8"));
				int strLen = hexString.length();
				String strLenHex = Integer.toHexString(strLen/2	);
				String hexLenPadded = StringUtils.leftPad(strLenHex, 64, '0');	
				String hexStrPadded = StringUtils.rightPad(hexString, 64, '0');	
				finalString = finalString+hexLenPadded+hexStrPadded;
			}
			catch (Exception e)
			{
				System.out.println("error creating hex");
				e.printStackTrace();
			}
		}

		return finalString;
	}
}
