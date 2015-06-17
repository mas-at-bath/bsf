package mygame;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetLoader;

import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;

/**
 *
 * @author vin
 */
public class TextLoader implements AssetLoader {
        ArrayList<String> lineContents = new ArrayList<String>();
    
        @Override
        public Object load(AssetInfo assetInfo) throws IOException 
        {
            Scanner scan = new Scanner(assetInfo.openStream());
            StringBuilder sb = new StringBuilder();
            try {
                while (scan.hasNextLine()) {
                    //sb.append(scan.nextLine()).append("\n");
                    lineContents.add(scan.nextLine());
                }
            } finally {
            scan.close();
            }
            return lineContents;
        }
 }
    

