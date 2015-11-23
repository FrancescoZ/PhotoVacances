package com.photovacances.francescozanoli.photovacances;

/**
 *Created by francescozanoli on 18/10/15.
        */

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.photovacances.francescozanoli.photovacances.Common.Pair;

import java.util.LinkedList;
import java.util.List;

//Le composant est utilisé par pour dessiner sur une photo, malheuressement le sauvgarde de la photo donne un errour donc il n'est pas utlise
public class DrawingView extends ImageView implements View.OnTouchListener{

    //Au debut le composant a été code pour pouvoir faire n'import quel dessin sur la photo mais seulement une partie du code a été utilisé
    //drawing path
    private Path drawPath;
    //drawing and canvas paint
    private Paint drawPaint, canvasPaint;
    //initial color
    private int paintColor = 0xFFFFFFFF;
    //canvas
    private Canvas drawCanvas;
    private Bitmap tmp;


    //on memorize le dernier et le premier point touché
    float firstTouchX,firstTouchY,lastTouchX,lastTouchY;
    //list des lignes dessinèès avant, la list contienne le deux point de debut et fin et la coleur du brush
    List<Pair<
            Pair<Pair<Float,Float>,Pair<Float,Float>>,
            Integer>> points;
    //Code pour dessiné plusieur que de point
    List<Pair<Pair<Float,Float>,Integer>> cas;

    public DrawingView(Context context, AttributeSet attrs){
        super(context, attrs);
        points=new LinkedList<>();
        cas=new LinkedList<>();
        setupDrawing();
    }

    //setup drawing
    private void setupDrawing(){

        //prepare for drawing and setup paint stroke properties
        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(20);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    //size assigned to view
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    //draw the view - will be called after touch event
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawCanvas=canvas;
        //Chaque triple de point,point coleur on l'affiche
        for (Pair<Pair<Pair<Float,Float>,Pair<Float,Float>>,Integer> ligne:points
             ) {
            Paint p=new Paint();
            p.setColor(ligne.getSecond());
            p.setAntiAlias(true);
            p.setStrokeWidth(20);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeJoin(Paint.Join.ROUND);
            p.setStrokeCap(Paint.Cap.ROUND);
            canvas.drawLine(
                    ligne.getFirst().getFirst().getFirst(),ligne.getFirst().getFirst().getSecond(),
                    ligne.getFirst().getSecond().getFirst(),ligne.getFirst().getSecond().getSecond(),
                    p);
        }
        //on enregistre la cache pour pouvoir enregistrer la photo
        tmp=getDrawingCache();
    }

    //register user touches as drawing action
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        //respond to down, move and up events
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                firstTouchX=touchX;
                firstTouchY=touchY;
                cas.add(new Pair<>(new Pair<>(touchX,touchY),drawPaint.getColor()));
                break;
            //quand on bouge
            case MotionEvent.ACTION_MOVE:
                cas.add(new Pair<>(new Pair<>(touchX,touchY),drawPaint.getColor()));
                break;
            //quand on arrete de dessine
            case MotionEvent.ACTION_UP:
                lastTouchX=touchX;
                lastTouchY=touchY;
                points.add(new Pair<>(
                        new Pair<>(
                            new Pair<>(firstTouchX,firstTouchY),
                            new Pair<>(lastTouchX,lastTouchY)),
                        drawPaint.getColor()));
                firstTouchY=lastTouchY;
                firstTouchX=lastTouchX;
                cas.add(new Pair<>(new Pair<>(touchX,touchY),drawPaint.getColor()));
                break;
            default:
                return false;
        }
        //redraw
        invalidate();
        return true;

    }

    //update color
    public void setColor(String newColor){
        invalidate();
        paintColor = Color.parseColor(newColor);
        drawPaint.setColor(paintColor);
    }

    @Override
    //Meme chose
    public boolean onTouch(View v, MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        //respond to down, move and up events
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                firstTouchX=touchX;
                firstTouchY=touchY;
                break;
            case MotionEvent.ACTION_MOVE:
                cas.add(new Pair<>(new Pair<>(event.getRawX(),event.getRawY()),drawPaint.getColor()));
                break;
            case MotionEvent.ACTION_UP:
                lastTouchX=touchX;
                lastTouchY=touchY;
                points.add(new Pair<>(
                        new Pair<>(
                                new Pair<>(firstTouchX,firstTouchY),
                                new Pair<>(lastTouchX,lastTouchY)),
                        drawPaint.getColor()));
                firstTouchY=lastTouchY;
                firstTouchX=lastTouchX;
                break;
            default:
                return false;
        }
        //redraw
        invalidate();
        return true;
    }

    //Effacer le dernier dessin
    public void eraseLast() {
        if (points.size()>0)
            points.remove(points.size()-1);
        invalidate();
    }

    //Enregister la photo modifie, ça marche pas
    public Bitmap getBitmap(Bitmap base,Context gContext){


        Resources resources = gContext.getResources();
        Bitmap bitmap =base;

        android.graphics.Bitmap.Config bitmapConfig =
                bitmap.getConfig();
        // set default bitmap config if none
        if(bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        base = base.copy(bitmapConfig, true);

        //je redessin le ligne sur la photo
        Canvas canvas = new Canvas(base);
        for (Pair<Pair<Pair<Float,Float>,Pair<Float,Float>>,Integer> ligne:points
                ) {
            Paint p=new Paint();
            p.setColor(ligne.getSecond());
            p.setAntiAlias(true);
            p.setStrokeWidth(20);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeJoin(Paint.Join.ROUND);
            p.setStrokeCap(Paint.Cap.ROUND);
            canvas.drawLine(
                    ligne.getFirst().getFirst().getFirst(),ligne.getFirst().getFirst().getSecond(),
                    ligne.getFirst().getSecond().getFirst(),ligne.getFirst().getSecond().getSecond(),
                    p);
        }

        return bitmap;
    }
}
