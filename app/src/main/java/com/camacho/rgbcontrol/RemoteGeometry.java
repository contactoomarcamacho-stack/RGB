package com.camacho.rgbcontrol;

/** Screen-sized remote body; six rows and four columns, with circular keys. */
public final class RemoteGeometry {
    public final float left, top, width, height, cellWidth, cellHeight, gridLeft, gridTop;
    public final int keySize;
    public RemoteGeometry(int screenWidth,int screenHeight,int insetLeft,int insetTop,int insetRight,int insetBottom) {
        float availableWidth=Math.max(1,screenWidth-insetLeft-insetRight);
        float availableHeight=Math.max(1,screenHeight-insetTop-insetBottom);
        width=availableWidth*0.98f;
        height=availableHeight*0.98f;
        left=insetLeft+(availableWidth-width)/2;
        top=insetTop+(availableHeight-height)/2;
        gridLeft=left+width*0.045f;
        gridTop=top+height*0.035f;
        cellWidth=width*0.91f/4;
        cellHeight=height*0.93f/6;
        keySize=Math.max(1,Math.round(Math.min(cellWidth*0.83f,cellHeight*0.84f)));
    }
    public int keyLeft(int action) {
        return Math.round(gridLeft+(action%4)*cellWidth+(cellWidth-keySize)/2);
    }
    public int keyTop(int action) {
        return Math.round(gridTop+(action/4)*cellHeight+(cellHeight-keySize)/2);
    }
}
