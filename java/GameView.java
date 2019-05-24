package com.example.mazeapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

import static com.example.mazeapp.frameproc.gesture;
import static com.example.mazeapp.frameproc.received;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class GameView extends View {
    public class innerclass extends Activity{
        Runnable myRunnable = new Runnable() {
            @Override
            public void run(){
                while(TRUE) {
                    while (apithread.done == 0) ;
                    input = gesture;
                    received = 1;
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run() {
                            if(input.equals("forward")){
                                input = "none";
                                movePlayer(Direction.UP);
                            }
                            if(input.equals("backward")){
                                input = "none";
                                movePlayer(Direction.DOWN);
                            }
                            if(input.equals("left")){
                                input = "none";
                                movePlayer(Direction.LEFT);
                            }
                            if(input.equals("right")){
                                input = "none";
                                movePlayer(Direction.RIGHT);
                            }
                        }
                    });
                }
            }
        };
        Thread inputthread = new Thread(myRunnable);
        public innerclass(){
            inputthread.start();
        }
    }
    private Cell[][] cells;
    private Cell player, goal;

    private static final int COLS=4, ROWS=4;
    private static final float wallThick = 7;
    private float cellSize, hMargin, wMargin;
    private Paint wallPaint, playerPaint, goalPaint;
    private Random randomIndex;
    String input = "none";

    private enum Direction{
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        innerclass A = new innerclass();

        wallPaint = new Paint();
        wallPaint.setColor(Color.BLACK);
        wallPaint.setStrokeWidth(wallThick);

        playerPaint = new Paint();
        playerPaint.setColor(Color.GREEN);

        goalPaint = new Paint();
        goalPaint.setColor(Color.RED);

        randomIndex = new Random();
        createMaze();

    }

    private Cell getNeighbor(Cell cell){
        ArrayList<Cell> neighbors = new ArrayList<>();

        if(cell.col>0 && !cells[cell.col-1][cell.row].visited){
            neighbors.add(cells[cell.col-1][cell.row]);
        }
        if(cell.col<COLS-1 && !cells[cell.col+1][cell.row].visited){
            neighbors.add(cells[cell.col+1][cell.row]);
        }
        if(cell.row>0 && !cells[cell.col][cell.row-1].visited){
            neighbors.add(cells[cell.col][cell.row-1]);
        }
        if(cell.row<ROWS-1 && !cells[cell.col][cell.row+1].visited){
            neighbors.add(cells[cell.col][cell.row+1]);
        }

        if(neighbors.size()>0) {
            return neighbors.get(randomIndex.nextInt(neighbors.size()));
        }
        else{
            return null;
        }
    }

    private void removeWall(Cell cell1, Cell cell2){
        if(cell1.col == cell2.col){
            if(cell1.row > cell2.row){
                cell1.topWall = false;
                cell2.botWall = false;
            }
            else{
                cell1.botWall = false;
                cell2.topWall = false;
            }
        }
        else{
            if(cell1.col > cell2.col){
                cell1.leftWall = false;
                cell2.rightWall = false;
            }
            else{
                cell1.rightWall = false;
                cell2.leftWall = false;
            }
        }
    }

    private  void createMaze(){
        Stack<Cell> stack = new Stack<>();
        Cell current, next;


        cells = new Cell[COLS][ROWS];

        for(int x=0; x<COLS; x++){
            for(int y=0; y<ROWS; y++){
                cells[x][y] = new Cell(x, y);
            }
        }

        player = cells[0][0];
        goal = cells[COLS-1][ROWS-1];


        current = cells[0][0];
        current.visited = true;
        do {
            next = getNeighbor(current);
            if (next != null) {
                removeWall(current, next);
                stack.push(current);
                current = next;
                current.visited = true;
            } else {
                current = stack.pop();
            }
        }while(!stack.empty());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);

        int width = getWidth();
        int height = getHeight();

        if(width/height > COLS/ROWS){
            cellSize = height/(ROWS+1);
        }
        else{
            cellSize = width/(COLS+1);
        }

        wMargin = (width - (COLS*cellSize))/2;
        hMargin = (height - (ROWS*cellSize))/2;

        canvas.translate(wMargin, hMargin);

        for(int x=0; x<COLS; x++){
            for(int y=0; y<ROWS; y++){
                if(cells[x][y].topWall){
                    canvas.drawLine(
                            x*cellSize,
                            y*cellSize,
                            (x+1)*cellSize,
                            y*cellSize,
                            wallPaint
                    );
                }

                if(cells[x][y].leftWall){
                    canvas.drawLine(
                            x*cellSize,
                            y*cellSize,
                            x*cellSize,
                            (y+1)*cellSize,
                            wallPaint
                    );
                }

                if(cells[x][y].rightWall){
                    canvas.drawLine(
                            (x+1)*cellSize,
                            y*cellSize,
                            (x+1)*cellSize,
                            (y+1)*cellSize,
                            wallPaint
                    );
                }

                if(cells[x][y].botWall){
                    canvas.drawLine(
                            x*cellSize,
                            (y+1)*cellSize,
                            (x+1)*cellSize,
                            (y+1)*cellSize,
                            wallPaint
                    );
                }
            }
        }

        float padding = cellSize/10;


        canvas.drawCircle(
                (player.col+0.5f)*cellSize,
                (player.row+0.5f)*cellSize,
                0.4f*cellSize,
                playerPaint
        );

        canvas.drawRect(
                (goal.col*cellSize)+padding,
                (goal.row*cellSize)+padding,
                ((goal.col+1)*cellSize)-padding,
                ((goal.row+1)*cellSize)-padding,
                goalPaint
        );
    }


    private void movePlayer(Direction direction){
        switch (direction){
            case UP:
                if(!player.topWall) {
                    player = cells[player.col][player.row - 1];
                }
                break;
            case DOWN:
                if(!player.botWall) {
                    player = cells[player.col][player.row + 1];
                }
                break;
            case LEFT:
                if(!player.leftWall) {
                    player = cells[player.col-1][player.row];
                }
                break;
            case RIGHT:
                if(!player.rightWall) {
                    player = cells[player.col+1][player.row];
                }
        }

        checkGoal();
        invalidate();
    }

    private void checkGoal(){
        if(player == goal){
            createMaze();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            return true;
        }

        if(event.getAction() == MotionEvent.ACTION_MOVE){
            float x = event.getX();
            float y = event.getY();

            float playerCenterX = wMargin + (player.col+0.5f)*cellSize;
            float playerCenterY = hMargin + (player.row+0.5f)*cellSize;

            float dx = x - playerCenterX;
            float dy = y - playerCenterY;

            if(Math.abs(dx) > cellSize || Math.abs(dy) > cellSize){
                if(Math.abs(dx) > Math.abs(dy)){
                    if(dx>0){
                        movePlayer(Direction.RIGHT);
                    }
                    else{
                        movePlayer(Direction.LEFT);
                    }
                }
                else{
                    if(dy>0){
                        movePlayer(Direction.DOWN);
                    }
                    else{
                        movePlayer(Direction.UP);
                    }
                }
            }
            return true;
        }
        return super.onTouchEvent(event);
    }

    private class Cell{
        boolean
                topWall =true,
                leftWall=true,
                botWall=true,
                rightWall=true,
                visited=false;

        int col, row;

        public Cell(int col, int row) {
            this.col = col;
            this.row = row;
        }
    }
}