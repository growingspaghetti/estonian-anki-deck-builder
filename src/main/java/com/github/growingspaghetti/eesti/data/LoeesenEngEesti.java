package com.github.growingspaghetti.eesti.data;

/**
 * @author ryoji
 */
public class LoeesenEngEesti {
    final private String eng;
    final private String eesti;
    final private String mp3;
    final private String img;
    final private String dataId;

    /**
     * @return possibly 🌵 if this entry is a variation of the former one.
     */
    public String getEng() {
        return eng;
    }
    public String getEesti() {
        return eesti;
    }
    /**
     * @return full path
     */
    public String getMp3() {
        return mp3;
    }
    /**
     * @return relative path. add prefix https://www.loecsen.com
     */
    public String getImg() {
        return img;
    }

    public String getDataId() {
        return dataId;
    }

    public LoeesenEngEesti(String eng, String eesti, String mp3, String img, String dataId) {
        this.eng    = eng;
        this.eesti  = eesti;
        this.mp3    = mp3;
        this.img    = img;
        this.dataId = dataId;
    }
}
