ArrayList<Punkt> pt= new ArrayList<Punkt>();
void setup (){
  fullScreen ();
  ellipseMode (CENTER);
  frameRate (2);
  fillList ();
}
int step = 0;
float b = 0.2;
void draw (){
  //step=5;
  background (0);
  for (Punkt p:pt) {
    p.display(step);
  }
  
  step++;
  //step = 4;
  if (step >= 5){
    step = 0;
    b += 0.1;
    if (b > 1) {
      b = 0.2;
      while (true){
        
      }
    }
    fillList ();
  }
}

void fillList () {
  pt.clear ();
  for (int i=0; i < 5; i++) {
    //println ((width/2.0)-((i/5.0)*width/2.0));
    pt.add (new Punkt ((width/2.0)-((i/4.0)*width/2.0*b), height/2, i));
    pt.add (new Punkt ((width/2.0)+((i/4.0)*width/2.0*b), height/2, i));
  }
}

class Punkt {
  float x,y;
  int schritt;
  Punkt (float _x, float _y, int s){
    x=_x;
    y=_y;
    schritt=s;
  }
  void display(int s) {
    if (s >= schritt) {
    fill (255);
    noStroke ();
    ellipse (x, y, 10, 10);
    }
  }
}