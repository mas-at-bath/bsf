/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.scene.Spatial;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;

/**
 *
 * @author vin
 */
public class EnvTerrain {
    
    private TerrainQuad myTerrain = new TerrainQuad();
    Material mat_terrain;
    
        EnvTerrain(AssetManager assetMan)
        {
            System.out.println("starting terrain");
        //TerrainQuad myTerrain;

        mat_terrain = new Material(assetMan, 
            "Common/MatDefs/Terrain/Terrain.j3md");
        mat_terrain.setTexture("Alpha", assetMan.loadTexture(
            "Textures/Terrain/splat/alphamap.png"));
        Texture grass = assetMan.loadTexture(
            "Textures/Terrain/splat/grass.jpg");
        grass.setWrap(WrapMode.Repeat);
        mat_terrain.setTexture("Tex1", grass);
        mat_terrain.setFloat("Tex1Scale", 64f);
        Texture dirt = assetMan.loadTexture(
            "Textures/Terrain/splat/dirt.jpg");
        dirt.setWrap(WrapMode.Repeat);
        mat_terrain.setTexture("Tex2", dirt);
        mat_terrain.setFloat("Tex2Scale", 32f);
        Texture rock = assetMan.loadTexture(
            "Textures/Terrain/splat/road.jpg");
        rock.setWrap(WrapMode.Repeat);
        mat_terrain.setTexture("Tex3", rock);
        mat_terrain.setFloat("Tex3Scale", 128f);
        AbstractHeightMap heightmap = null;
        Texture heightMapImage = assetMan.loadTexture(
            "Textures/Terrain/splat/mountains512.png");
        heightmap = new ImageBasedHeightMap(heightMapImage.getImage());
        heightmap.setHeightScale(0.05f);
        heightmap.load();
        heightmap.setHeightScale(0.05f);
        int patchSize = 65;
        myTerrain = new TerrainQuad("my terrain", patchSize, 513, heightmap.getHeightMap());
        myTerrain.setMaterial(mat_terrain);
        myTerrain.setLocalTranslation(0, -10, 0);
        myTerrain.setLocalScale(20f, 1f, 20f);
        
        System.out.println("finished terrain");
        //return myTerrain;
    }
        
        public TerrainQuad getTerrain()
        {
            return myTerrain;
        }
    
}
