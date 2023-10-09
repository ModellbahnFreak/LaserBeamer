float t = 1.5;
float speed = 0.03;
void setup () {
  fullScreen ();
  ellipseMode (CENTER);
}
void draw () {
  background (0);
  fill (0, 255, 0);
  t += speed;
  if (t >= 3.5 || t <= 1.5) {
    speed = -speed;
  }
  float y = 0.5*sin(t*PI)+0.5;
  rect ((width/2)-(y*width/2), (height/2)-10/2, y*width, 10);
  fill (255,0,0);
  ellipse ((width/2)-(y*width/2), (height/2), 20, 10);
  ellipse ((width/2)+(y*width/2), (height/2), 20, 10);
}