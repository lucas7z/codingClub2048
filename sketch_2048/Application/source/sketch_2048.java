import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class sketch_2048 extends PApplet {


class Player { 
  long fitness;
  boolean dead = false;
  int score =0;

  ArrayList<Tile> tiles = new ArrayList<Tile>();
  ArrayList<PVector> emptyPositions = new ArrayList<PVector>();
  PVector moveDirection = new PVector();
  boolean movingTheTiles =false;
  boolean tileMoved = false;

  float[][] startingPositions = new float[2][3];

  State start;

  Player() {
    fillEmptyPositions(); 

    addNewTile();
    addNewTile();

    startingPositions[0][0] = tiles.get(0).position.x;
    startingPositions[0][1] = tiles.get(0).position.y;
    startingPositions[0][2] = tiles.get(0).value;


    startingPositions[1][0] = tiles.get(1).position.x;
    startingPositions[1][1] = tiles.get(1).position.y;
    startingPositions[1][2] = tiles.get(1).value;
  }

  Player(boolean isReplay) {
    fillEmptyPositions();
  }


  public void setTilesFromHistory() {

    tiles.add(new Tile(floor(startingPositions[0][0]), floor(startingPositions[0][1])));
    tiles.get(0).value = floor(startingPositions[0][2]);

    tiles.add(new Tile(floor(startingPositions[1][0]), floor(startingPositions[1][1])));
    tiles.get(1).value = floor(startingPositions[1][2]);


    for ( int i = 0; i< emptyPositions.size(); i ++) {
      if (compareVec(emptyPositions.get(i), tiles.get(0).position) || compareVec(emptyPositions.get(i), tiles.get(1).position)) {
        emptyPositions.remove(i);
        i--;
      }
    }
  }

  public void show() {
    for (int i = 0; i< tiles.size(); i++) {
      if (tiles.get(i).deathOnImpact) {
        tiles.get(i).show();
      }
    }

    for (int i = 0; i< tiles.size(); i++) {
      if (!tiles.get(i).deathOnImpact) {
        tiles.get(i).show();
      }
    }
  }

  public void move() {
    if (movingTheTiles) {
      for (int i = 0; i< tiles.size(); i++) {
        tiles.get(i).move(moveSpeed);
      }
      if (doneMoving()) {
        for (int i = 0; i< tiles.size(); i++) {//kill collided tiles
          if (tiles.get(i).deathOnImpact) {
            tiles.remove(i);
            i--;
          }
        }

        movingTheTiles =false;
        setEmptyPositions();
        addNewTileNotRandom();
      }
    }
  }

  public boolean doneMoving() {

    for (int i = 0; i< tiles.size(); i++) {
      if (tiles.get(i).moving) {
        return false;
      }
    }

    return true;
  }

  public void update() {
    move();
  }

  public void fillEmptyPositions() {
    for (int i = 0; i< 4; i++) {
      for (int j =0; j< 4; j++) {
        emptyPositions.add(new PVector(i, j));
      }
    }
  }

  public void setEmptyPositions() {
    emptyPositions.clear();
    for (int i = 0; i< 4; i++) {
      for (int j =0; j< 4; j++) {
        if (getValue(i, j) ==0) {
          emptyPositions.add(new PVector(i, j));
        }
      }
    }
  }

  public void moveTiles() {
    tileMoved = false;
    for (int i = 0; i< tiles.size(); i++) {
      tiles.get(i).alreadyIncreased = false;
    }
    ArrayList<PVector> sortingOrder = new ArrayList<PVector>();
    PVector sortingVec = new PVector();
    boolean vert = false;
    if (moveDirection.x ==1) {
      sortingVec = new PVector(3, 0);
      vert = false;
    } else if (moveDirection.x ==-1) {
      sortingVec = new PVector(0, 0);
      vert = false;
    } else if (moveDirection.y ==1) {
      sortingVec = new PVector(0, 3);
      vert = true;
    } else if (moveDirection.y ==-1) {
      sortingVec = new PVector(0, 0);
      vert = true;
    }

    for (int i = 0; i< 4; i++) {
      for (int j = 0; j<4; j++) {
        PVector temp = new PVector(sortingVec.x, sortingVec.y);
        if (vert) {
          temp.x += j;
        } else {
          temp.y += j;
        }
        sortingOrder.add(temp);
      }
      sortingVec.sub(moveDirection);
    }

    for (int j = 0; j< sortingOrder.size(); j++) {
      for (int i = 0; i< tiles.size(); i++) {
        if (tiles.get(i).position.x == sortingOrder.get(j).x && tiles.get(i).position.y == sortingOrder.get(j).y) {
          PVector moveTo = new PVector(tiles.get(i).position.x + moveDirection.x, tiles.get(i).position.y + moveDirection.y);
          int valueOfMoveTo = getValue(floor(moveTo.x), floor(moveTo.y));
          while (valueOfMoveTo == 0) {
            tiles.get(i).moveTo(moveTo);
            moveTo = new PVector(tiles.get(i).positionTo.x + moveDirection.x, tiles.get(i).positionTo.y + moveDirection.y);
            valueOfMoveTo = getValue(floor(moveTo.x), floor(moveTo.y));
            tileMoved = true;
          }

          if (valueOfMoveTo == tiles.get(i).value) {
            Tile temp = getTile(floor(moveTo.x), floor(moveTo.y));



            if (!temp.alreadyIncreased) {

              tiles.get(i).moveTo(moveTo);
              tiles.get(i).deathOnImpact = true;


              temp.alreadyIncreased = true;
              tiles.get(i).alreadyIncreased = true;
              temp.value *=2;
              score += temp.value;
              temp.setColour();
              tileMoved = true;
            }
          }
        }
      }
    }
    if (tileMoved) {
      movingTheTiles = true;
    }
  }

  public void addNewTile() {


    PVector temp = emptyPositions.remove(floor(random(emptyPositions.size())));
    tiles.add(new Tile(floor(temp.x), floor(temp.y)));
  }


  public void addNewTileNotRandom() {
    int notRandomNumber = score;
    for (int i = 0; i< tiles.size(); i++) {
      notRandomNumber += floor(tiles.get(i).position.x);
      notRandomNumber += floor(tiles.get(i).position.y);
      notRandomNumber += i;
    }

    int notRandomNumber2 = notRandomNumber %  emptyPositions.size();
    PVector temp = emptyPositions.remove(notRandomNumber2);
    tiles.add(new Tile(floor(temp.x), floor(temp.y)));

    if (notRandomNumber % 10 < 9) {
      tiles.get(tiles.size() -1).value = 2;
    } else {
      tiles.get(tiles.size() -1).value = 4;
    }

    tiles.get(tiles.size() -1).setColour();
  }


  public int getValue(int x, int y) {
    if (x > 3 || x <0 || y>3 || y<0) { 
      return -1;
    }
    for (int i = 0; i< tiles.size(); i++) {
      if (tiles.get(i).positionTo.x == x && tiles.get(i).positionTo.y ==y) {
        return tiles.get(i).value;
      }
    }
    return 0;
  }

  public Tile getTile(int x, int y) {
    for (int i = 0; i< tiles.size(); i++) {
      if (tiles.get(i).positionTo.x == x && tiles.get(i).positionTo.y ==y) {
        return tiles.get(i);
      }
    }

    return null;
  }


  public void setStartState() {
    start = new State();
    for (int i = 0; i< tiles.size(); i++) {
      start.tiles.add(tiles.get(i).clone());
    }
    start.score = score;
    start.setEmptyPositions();
  }

  public void getMove() {
    setStartState();
    start.getChildrenValues(0);
    switch(start.bestChild) {
    case 0 :
      moveDirection = new PVector(1, 0);
      break;
    case 1 :
      moveDirection = new PVector(-1, 0);
      break;
    case 2 :
      moveDirection = new PVector(0, 1);
      break;
    case 3 :
      moveDirection = new PVector(0, -1);
      break;
      
      
    }
    
    if(start.children[start.bestChild].value <= 0){
     setup(); 
    }
  }
}
Player p;
boolean released = true;
boolean teleport = false;

int maxDepth = 4;
int pauseCounter = 100;
int nextConnectionNo = 1000;
int speed = 60;
int moveSpeed = 60;

int numberCase = 4;

int xoffset = 0;
int yoffset = 0;

public void settings() {
  size(850, 850000);
}

public void setup() {
  frameRate(60);
  p = new Player();
}



public void draw() {
  background(187,173,160);
  fill(205,193,180);
  for(int i = 0 ; i< 4 ;i++){
    for(int j =0; j< 4 ;j++){
      rect(i*200 + (i+1) *10, j*200 + (j+1) *10, 200, 200);  
    }
    
    
  }
  p.move();
  p.show();
  
  if(p.doneMoving()){
    p.getMove();
  }
}


public void keyPressed() {
  if (released) {
    if(key == CODED){
      switch(keyCode){
        case UP:
          p.moveDirection = new PVector(0,-1);
          p.moveTiles();
          break;
          
        case DOWN:
          p.moveDirection = new PVector(0,1);
          p.moveTiles();
          break;
        
        case RIGHT:
          p.moveDirection = new PVector(1,0);
          p.moveTiles();
          break;
   
        case LEFT:
          p.moveDirection = new PVector(-1,0);
          p.moveTiles();
          break;
          
        default:
          break;
      }
      
    }
  }
}

public void keyReleased(){
 released = true; 
}

public boolean compareVec(PVector p1, PVector p2) {
  if (p1.x == p2.x && p1.y == p2.y) {
    return true;
  }
  return false;
}
class State {
  State[] children = new State[4];
  float value = 0;
  int score = 0;

  ArrayList<Tile> tiles = new ArrayList<Tile>();
  ArrayList<PVector> emptyPositions = new ArrayList<PVector>();
  boolean differentFromParent = false;
  boolean dead =false;
  int bestChild =0;
  
  
  State() {
  }



  public State clone() {
    State clone = new State();
    //clone tiles
    for (int i = 0; i< tiles.size(); i++) {
      clone.tiles.add(tiles.get(i).clone());
    }
    clone.score = score;
    clone.setEmptyPositions();
    return clone;
  }

  public void fillEmptyPositions() {
    for (int i = 0; i< 4; i++) {
      for (int j =0; j< 4; j++) {
        emptyPositions.add(new PVector(i, j));
      }
    }
  }

  public void setEmptyPositions() {
    emptyPositions.clear();
    for (int i = 0; i< 4; i++) {
      for (int j =0; j< 4; j++) {
        if (getValue(i, j) ==0) {
          emptyPositions.add(new PVector(i, j));
        }
      }
    }
  }


  public void move(int x, int y) {
    PVector moveDirection = new PVector(x, y);
    differentFromParent = false;
    for (int i = 0; i< tiles.size(); i++) {
      tiles.get(i).alreadyIncreased = false;
    }
    ArrayList<PVector> sortingOrder = new ArrayList<PVector>();
    PVector sortingVec = new PVector();
    boolean vert = false;
    if (x ==1) {
      sortingVec = new PVector(3, 0);
      vert = false;
    } else if (x ==-1) {
      sortingVec = new PVector(0, 0);
      vert = false;
    } else if (y ==1) {
      sortingVec = new PVector(0, 3);
      vert = true;
    } else if (y ==-1) {
      sortingVec = new PVector(0, 0);
      vert = true;
    }
    for (int i = 0; i< 4; i++) {
      for (int j = 0; j<4; j++) {
        PVector temp = new PVector(sortingVec.x, sortingVec.y);
        if (vert) {
          temp.x += j;
        } else {
          temp.y += j;
        }
        sortingOrder.add(temp);
      }
      sortingVec.sub(moveDirection);
    }

    for (int j = 0; j< sortingOrder.size(); j++) {
      for (int i = 0; i< tiles.size(); i++) {
        if (tiles.get(i).position.x == sortingOrder.get(j).x && tiles.get(i).position.y == sortingOrder.get(j).y) {
          PVector moveTo = new PVector(tiles.get(i).position.x + moveDirection.x, tiles.get(i).position.y + moveDirection.y);
          int valueOfMoveTo = getValue(floor(moveTo.x), floor(moveTo.y));
          while (valueOfMoveTo == 0) {
            tiles.get(i).moveToNow(moveTo);
            moveTo = new PVector(tiles.get(i).position.x + moveDirection.x, tiles.get(i).position.y + moveDirection.y);
            valueOfMoveTo = getValue(floor(moveTo.x), floor(moveTo.y));
            differentFromParent = true;
          }

          if (valueOfMoveTo == tiles.get(i).value) {
            Tile temp = getTile(floor(moveTo.x), floor(moveTo.y));



            if (!temp.alreadyIncreased) {

              tiles.get(i).moveToNow(moveTo);
              tiles.get(i).deathOnImpact = true;


              //tiles.remove(i);
              temp.alreadyIncreased = true;
              tiles.get(i).alreadyIncreased = true;
              temp.value *=2;
              score += temp.value;
              temp.setColour();
              differentFromParent = true;
            }
          }
        }
      }
    }
    if (differentFromParent) {

      addNewTileNotRandom();
    } else {
      dead= true;
    }
  }

  public void addNewTile() {


    PVector temp = emptyPositions.remove(floor(random(emptyPositions.size())));
    tiles.add(new Tile(floor(temp.x), floor(temp.y)));
  }


  public void addNewTileNotRandom() {

    setEmptyPositions();
    if (emptyPositions.size() ==0) {
      dead = true; 
      return;
    }
    int notRandomNumber = score;
    for (int i = 0; i< tiles.size(); i++) {
      notRandomNumber += floor(tiles.get(i).position.x);
      notRandomNumber += floor(tiles.get(i).position.y);
      notRandomNumber += i;
    }

    int notRandomNumber2 = notRandomNumber %  emptyPositions.size();
    PVector temp = emptyPositions.remove(notRandomNumber2);
    tiles.add(new Tile(floor(temp.x), floor(temp.y)));

    if (notRandomNumber % 10 < 9) {
      tiles.get(tiles.size() -1).value = 2;
    } else {
      tiles.get(tiles.size() -1).value = 4;
    }

    tiles.get(tiles.size() -1).setColour();
  }

  public int getValue(int x, int y) {
    if (x > 3 || x <0 || y>3 || y<0) { 
      return -1;
    }
    for (int i = 0; i< tiles.size(); i++) {
      if (tiles.get(i).position.x == x && tiles.get(i).position.y ==y) {
        return tiles.get(i).value;
      }
    }
    return 0;
  }


  public Tile getTile(int x, int y) {
    for (int i = 0; i< tiles.size(); i++) {
      if (tiles.get(i).position.x == x && tiles.get(i).position.y ==y) {
        return tiles.get(i);
      }
    }
    return null;
  }

  public void setValue() {
    if (dead) {
      value =0;
      return;
    }
    value = 0;
    value += score;
    value += random(1000);
    
  }

  public float getChildrenValues(int depth) {
    if (depth >= maxDepth) {
      setValue();
      return value;
    }

    for (int i = 0; i< 4; i++) {
      children[i] = clone();
    }

    children[0].move(1, 0);

    children[1].move(-1, 0);

    children[2].move(0, 1);

    children[3].move(0, -1);

    for (int i = 0; i< 4; i++) {
      if (!children[i].differentFromParent) {
        children[i].dead = true;
      }
    }



    float max = 0;
    int maxChild =0;
    for (int i = 0; i  < 4; i++) {
      if (!children[i].dead) {
        float temp = children[i].getChildrenValues(depth+1) ;

        if (depth ==0) {
        }
        if (temp > max) {
          max =  temp;
          maxChild = i;
        }
      }
    }


    bestChild = maxChild;
    
    setValue();
    if(max < value){
     return value; 
    }
    return max;
  }
}
class Tile {
  int value;
  PVector position;
  PVector pixelPos;//top left
  boolean alreadyIncreased = false;
  boolean moving = false;

  int colour;
  PVector positionTo;
  PVector pixelPosTo;
  HashMap<Integer, int[]> map;

  boolean deathOnImpact =false;

  Tile(int x, int y) {
    map = new HashMap();
    if (random(1)< 0.1f) {
      value = 4;
    } else {
      value =2;
    }

    position = new PVector(x, y);
    positionTo = new PVector(x, y);

    pixelPos = new PVector(xoffset +x*200 + (x+1) *10, yoffset + y*200 + (y+1) *10);
    pixelPosTo = new PVector(xoffset + x*200 + (x+1) *10, yoffset + y*200 + (y+1) *10);
    setAllColor();
    setColour();
  }
  
  

  public void show() {
    if (!deathOnImpact || moving) {
      fill(colour);
      noStroke();
      rect(pixelPos.x, pixelPos.y, 200, 200);
      if (value < 2048) {
        fill(40);
      } else {
        fill(240);
      }
      textAlign(CENTER, CENTER);
      textSize(50);
      text(value, pixelPos.x+100, pixelPos.y+100);
    }
  }

  public void moveTo(PVector to) {
    positionTo = new PVector(to.x, to.y);
    pixelPosTo = new PVector(xoffset +to.x*200 + (to.x+1) *10, yoffset +to.y*200 + (to.y+1) *10);
    moving = true;
  }
  public void moveToNow(PVector to) {
    position = new PVector(to.x, to.y);
    pixelPos = new PVector(xoffset +to.x*200 + (to.x+1) *10, yoffset +to.y*200 + (to.y+1) *10);
  }

  public void move(int speed) {
    if (moving) {
      if (!teleport && dist(pixelPos.x, pixelPos.y, pixelPosTo.x, pixelPosTo.y) > speed) {
        PVector MoveDirection = new PVector(positionTo.x - position.x, positionTo.y - position.y); 
        MoveDirection.normalize();
        MoveDirection.mult(speed);
        pixelPos.add(MoveDirection);
      } else {
        moving = false;
        pixelPos = new PVector(pixelPosTo.x, pixelPosTo.y);
        position = new PVector(positionTo.x, positionTo.y);
      }
    }
  }
  
  public void setAllColor(){
    map.put(2,new int[] {0, 255, 80});
    map.put(4,new int[] {0, 255, 90});
    map.put(32,new int[] {0, 255, 100});
    map.put(64,new int[] {0, 255, 110});
    map.put(128,new int[] {10, 0, 120});
    map.put(256,new int[] {100, 0, 0});
    map.put(512,new int[] {150, 0, 0});
    map.put(1024,new int[] {0, 0, 10});
    map.put(2048,new int[] {0, 0, 20});
    map.put(4096,new int[] {0, 0, 40});
    map.put(8192,new int[] {0, 0, 80});
  }
  
  public void setColour() {
    try{
    int[] val = map.get(value);
    this.colour = color(val[0],val[1],val[2]);
    }catch(NullPointerException e){
      int[] val = map.get(2);
      this.colour = color(val[0],val[1],val[2]);
    }
  }

  public Tile clone() {
    Tile clone = new Tile(floor(position.x), floor(position.y));
    clone.value = value;
    clone.setColour();

    return clone;
  }
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "sketch_2048" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
