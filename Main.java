import java.util.*;
import javax.imageio.ImageIO;
import java.util.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;

public class Main extends JFrame {
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            Main ex = new Main();
            ex.setVisible(true);
        });
    }

    public Main() {
        initUI();
    }

    private void initUI() {
        add(new Game());

        setTitle("Snake Game");
        setSize(800, 610);

        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}

class Game extends JPanel {
    // La clase "Game" extiende de JPanel y representa el juego en sí.
    private Timer timer;
    private Snake snake;
    private Point cherry;
    private int points = 0;
    private int best = 0;
    private BufferedImage image;
    private GameStatus status;
    private boolean didLoadCherryImage = true;

    // Fuentes utilizadas en la interfaz del juego
    private static Font FONT_M = new Font("DePixel", Font.PLAIN, 24);
    private static Font FONT_M_ITALIC = new Font("DePixel", Font.ITALIC, 24);
    private static Font FONT_L = new Font("DePixel", Font.PLAIN, 84);
    private static Font FONT_XL = new Font("DePixel", Font.PLAIN, 150);

    // Dimensiones del juego
    private static int WIDTH = 760;
    private static int HEIGHT = 520;

    // Retardo del juego (en milisegundos)
    private static int DELAY = 45;

    // Constructor
    public Game() {
        try {
            image = ImageIO.read(new File("cherry.png"));
        } catch (IOException e) {
            didLoadCherryImage = false;
        }

        addKeyListener(new KeyListener());
        setFocusable(true);
        setBackground(new Color(0, 0, 0));
        setDoubleBuffered(true);

        snake = new Snake(WIDTH / 2, HEIGHT / 2);
        status = GameStatus.TITTLE_GAME;
        repaint();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        render(g);

        Toolkit.getDefaultToolkit().sync();
    }

    // Actualiza el estado del juego
    private void update() {
        snake.move();

        if (cherry != null && snake.getHead().intersects(cherry, 20)) {
            snake.addTail();
            cherry = null;
            points++;
        }

        if (cherry == null) {
            spawnCherry();
        }

        checkForGameOver();
    }

    // Reinicia el juego
    private void reset() {
        points = 0;
        cherry = null;
        snake = new Snake(WIDTH / 2, HEIGHT / 2);
        setStatus(GameStatus.RUNNING);
    }

    // Establece el estado del juego
    private void setStatus(GameStatus newStatus) {
        switch (newStatus) {
            case RUNNING:
                timer = new Timer();
                timer.schedule(new GameLoop(), 0, DELAY);
                break;
            case PAUSED:
            case GAME_OVER:
                timer.cancel();
                best = points > best ? points : best;
                break;
            default:
                break;
        }

        status = newStatus;
    }

    // Alterna entre pausa y ejecución del juego
    private void togglePause() {
        setStatus(status == GameStatus.PAUSED ? GameStatus.RUNNING : GameStatus.PAUSED);
    }

    // Verifica si la serpiente ha golpeado una pared o a sí misma
    private void checkForGameOver() {
        Point head = snake.getHead();
        boolean hitBoundary = head.getX() <= 20
                || head.getX() >= WIDTH + 10
                || head.getY() <= 40
                || head.getY() >= HEIGHT + 30;

        boolean ateItself = false;

        for (Point t : snake.getTail()) {
            ateItself = ateItself || head.equals(t);
        }

        if (hitBoundary || ateItself) {
            setStatus(GameStatus.GAME_OVER);
        }
    }

    // Dibuja una cadena de texto centrada en el panel
    public void drawCenteredString(Graphics g, String text, Font font, int y) {
        FontMetrics metrics = g.getFontMetrics(font);
        int x = (WIDTH - metrics.stringWidth(text)) / 2;

        g.setFont(font);
        g.drawString(text, x, y);
    }

    // Dibuja los elementos del juego en el panel
    private void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.WHITE);
        g2d.setFont(FONT_M);

        if (status == GameStatus.TITTLE_GAME) {
            // g2d.setFont(FONT_L);

            drawCenteredString(g2d, "SNAKE", FONT_XL, 200);
            drawCenteredString(g2d, "GAME", FONT_XL, 300);
            drawCenteredString(g2d, "Press  any  key  to  begin", FONT_M_ITALIC, 330);

            return;

        }

        Point p = snake.getHead();

        g2d.drawString("SCORE: " + String.format("%02d", points), 20, 30);
        g2d.drawString("BEST: " + String.format("%02d", best), 630, 30);

        if (cherry != null) {
            if (didLoadCherryImage) {
                g2d.drawImage(image, cherry.getX(), cherry.getY(), 15, 15, null);
            } else {
                g2d.setColor(Color.GREEN);
                g2d.fillOval(cherry.getX(), cherry.getY(), 10, 10);
                g2d.setColor(Color.GREEN);
            }
        }

        if (status == GameStatus.GAME_OVER) {
            drawCenteredString(g2d, "Press  enter  to  start  again", FONT_M_ITALIC, 330);
            drawCenteredString(g2d, "GAME OVER", FONT_L, 300);
        }

        if (status == GameStatus.PAUSED) {
            // g2d.drawString("Paused", 600, 14);
            drawCenteredString(g2d, "Paused", FONT_L, 330);
        }

        // instrucción para cambiarle el color a la serpiente
        g2d.setColor(new Color(117, 192, 90));
        g2d.fillRect(p.getX(), p.getY(), 10, 10);

        for (int i = 0, size = snake.getTail().size(); i < size; i++) {
            Point t = snake.getTail().get(i);

            g2d.fillRect(t.getX(), t.getY(), 10, 10);
        }

        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(4));
        g2d.drawRect(20, 40, WIDTH, HEIGHT);
    }

    // Genera una cereza en una posición aleatoria
    public void spawnCherry() {
        cherry = new Point((new Random()).nextInt(WIDTH - 60) + 20,
                (new Random()).nextInt(HEIGHT - 60) + 40);
    }

    // Clase interna para manejar los eventos de teclado
    private class KeyListener extends KeyAdapter {
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            if (status == GameStatus.RUNNING) {
                switch (key) {
                    case KeyEvent.VK_A:
                        snake.turn(Direction.LEFT);
                        break;
                    case KeyEvent.VK_D:
                        snake.turn(Direction.RIGHT);
                        break;
                    case KeyEvent.VK_W:
                        snake.turn(Direction.UP);
                        break;
                    case KeyEvent.VK_S:
                        snake.turn(Direction.DOWN);
                        break;
                }
            }

            if (status == GameStatus.TITTLE_GAME) {
                setStatus(GameStatus.RUNNING);
            }

            if (status == GameStatus.GAME_OVER && key == KeyEvent.VK_ENTER) {
                reset();
            }

            if (key == KeyEvent.VK_P) {
                togglePause();
            }
        }
    }

    // Clase interna para el bucle principal del juego
    private class GameLoop extends java.util.TimerTask {
        public void run() {
            update();
            repaint();
        }
    }
}

enum GameStatus {
    // La clase "GameStatus" representa los posibles estados del juego.
    TITTLE_GAME, RUNNING, PAUSED, GAME_OVER
}

enum Direction {
    // La clase "Direction" representa las direcciones posibles de una serpiente.
    UP, DOWN, LEFT, RIGHT;

    // Este método verifica si la dirección es horizontal (eje x).
    public boolean isX() {
        // Comprueba si la dirección es izquierda o derecha.
        return this == LEFT || this == RIGHT;
    }

    // Este método verifica si la dirección es vertical (eje y).
    public boolean isY() {
        // Comprueba si la dirección es arriba o abajo.
        return this == UP || this == DOWN;
    }
}

class Point {
    // Esta clase representa un punto en un plano cartesiano con coordenadas x e y.
    private int x; // Almacena la coordenada x del punto.
    private int y; // Almacena la coordenada y del punto.

    // Este es el constructor de la clase, se ejecuta cuando se crea un nuevo objeto
    // "Point".
    public Point(int x, int y) {
        // Se asignan los valores de x e y pasados como parámetros a las variables de la
        // clase.
        this.x = x;
        this.y = y;
    }

    // Este es otro constructor de la clase que crea una copia de otro objeto
    // "Point".
    public Point(Point p) {
        // Se obtienen las coordenadas x e y del punto pasado como parámetro y se
        // asignan a las variables de la clase.
        this.x = p.getX();
        this.y = p.getY();
    }

    // Este método se llama para mover el punto en una dirección determinada y una
    // cantidad especificada.
    public void move(Direction d, int value) {
        // Se utiliza una estructura de control switch para determinar la dirección y
        // actualizar las coordenadas del punto.
        switch (d) {
            case UP:
                this.y -= value;
                break;
            case DOWN:
                this.y += value;
                break;
            case RIGHT:
                this.x += value;
                break;
            case LEFT:
                this.x -= value;
                break;
        }
    }

    // Este método devuelve la coordenada x del punto.
    public int getX() {
        return x;
    }

    // Este método devuelve la coordenada y del punto.
    public int getY() {
        return y;
    }

    // Este método establece la coordenada x del punto y devuelve el objeto "Point"
    // actualizado.
    public Point setX(int x) {
        this.x = x;

        return this;
    }

    // Este método establece la coordenada y del punto y devuelve el objeto "Point"
    // actualizado.
    public Point setY(int y) {
        this.y = y;

        return this;
    }

    // Este método compara si el punto actual es igual a otro punto pasado como
    // parámetro.
    public boolean equals(Point p) {
        // Compara las coordenadas x e y del punto actual con las coordenadas del punto
        // pasado como parámetro.
        return this.x == p.getX() && this.y == p.getY();
    }

    // Este método devuelve una representación en forma de cadena del punto.
    public String toString() {
        // Devuelve una cadena con las coordenadas x e y del punto en formato "(x, y)".
        return "(" + x + ", " + y + ")";
    }

    // Este método verifica si el punto actual intersecta con otro punto pasado como
    // parámetro.
    public boolean intersects(Point p) {
        // Llama al método "intersects" con el valor de tolerancia predeterminado (10).
        return intersects(p, 10);
    }

    // Este método verifica si el punto actual intersecta con otro punto pasado como
    // parámetro, con una tolerancia específica.
    public boolean intersects(Point p, int tolerance) {
        // Calcula la diferencia en las coordenadas x e y entre el punto actual y el
        // punto pasado como parámetro.
        int diffX = Math.abs(x - p.getX());
        int diffY = Math.abs(y - p.getY());

        // Verifica si las diferencias en las coordenadas x e y están dentro de la
        // tolerancia especificada o si los puntos son iguales.
        return this.equals(p) || (diffX <= tolerance && diffY <= tolerance);
    }
}

class Snake {
    // Esta clase tiene algunas variables privadas que almacenan información sobre
    // la serpiente.
    private Direction direction; // Almacena la dirección en la que se mueve la serpiente.
    private Point head; // Almacena la posición de la cabeza de la serpiente.
    private ArrayList<Point> tail; // Almacena las posiciones de la cola de la serpiente.

    // Este es el método constructor, se ejecuta cuando se crea una nueva serpiente.
    public Snake(int x, int y) {
        // Se crea un nuevo objeto "Point" para representar la posición de la cabeza de
        // la serpiente.
        this.head = new Point(x, y);
        // Se establece la dirección inicial de la serpiente hacia la derecha.
        this.direction = Direction.RIGHT;
        // Se crea una nueva lista para almacenar las posiciones de la cola de la
        // serpiente.
        this.tail = new ArrayList<Point>();

        // Se agregan tres puntos iniciales a la cola de la serpiente, todos en la
        // posición (0, 0).
        this.tail.add(new Point(0, 0));
        this.tail.add(new Point(0, 0));
        this.tail.add(new Point(0, 0));
    }

    // Este método se llama para mover la serpiente en una dirección determinada.
    public void move() {
        // Se crea una nueva lista para almacenar las nuevas posiciones de la cola de la
        // serpiente.
        ArrayList<Point> newTail = new ArrayList<Point>();

        // Se itera sobre todas las posiciones de la cola de la serpiente.
        for (int i = 0, size = tail.size(); i < size; i++) {
            // Se obtiene la posición anterior, que es la posición anterior en la lista o la
            // cabeza si es el primer punto.
            Point previous = i == 0 ? head : tail.get(i - 1);

            // Se agrega una nueva posición a la lista de la cola de la serpiente, con las
            // mismas coordenadas que la posición anterior.
            newTail.add(new Point(previous.getX(), previous.getY()));
        }

        // Se actualiza la lista de la cola de la serpiente con las nuevas posiciones.
        this.tail = newTail;

        // Se mueve la cabeza de la serpiente en la dirección actual.
        this.head.move(this.direction, 10);
    }

    // Este método se llama para agregar un nuevo punto a la cola de la serpiente.
    public void addTail() {
        // Se agrega un nuevo punto en una posición fuera de la pantalla (-10, -10).
        this.tail.add(new Point(-10, -10));
    }

    // Este método se llama para cambiar la dirección de la serpiente.
    public void turn(Direction d) {
        // Se verifica si la nueva dirección es perpendicular a la dirección actual.
        if (d.isX() && direction.isY() || d.isY() && direction.isX()) {
            // Si es así, se actualiza la dirección actual de la serpiente.
            direction = d;
        }
    }

    // Este método devuelve la lista de posiciones de la cola de la serpiente.
    public ArrayList<Point> getTail() {
        return this.tail;
    }

    // Este método devuelve la posición de la cabeza de la serpiente.
    public Point getHead() {
        return this.head;
    }
}
