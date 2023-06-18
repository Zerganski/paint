import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class SimplePaintApp extends JFrame {
    private JPanel canvas;
    private Color currentColor;
    private int brushSize;
    private List<List<Point>> paths;
    private List<List<Point>> undonePaths;
    private List<Color> pathColors;
    private List<Color> undonePathColors;
    private List<Integer> pathSizes;
    private List<Integer> undonePathSizes;
    private JMenuItem saveMenuItem;
    private JMenuItem undoMenuItem;
    private JMenuItem redoMenuItem;
    public SimplePaintApp() {
        setTitle("Prosty Paint");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());


        canvas = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                for (int i = 0; i < paths.size(); i++) {
                    List<Point> path = paths.get(i);
                    Color color = pathColors.get(i);
                    int size = pathSizes.get(i);
                    drawPath(g2d, path, color, size);

                }
            }
        };
        canvas.setPreferredSize(new Dimension(800, 600));
        setSize(canvas.getPreferredSize());
        setLocationRelativeTo(null);
        setVisible(true);
        canvas.setBackground(Color.WHITE);
        canvas.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                startPath(e.getX(), e.getY());
            }

            public void mouseReleased(MouseEvent e) {
                endPath();
            }
        });
        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                addPointToPath(e.getX(), e.getY());
            }
        });
        add(canvas, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        JButton colorButton = new JButton("Wybierz kolor");
        colorButton.setBackground(Color.PINK); // Kolor tła
        colorButton.setForeground(Color.BLACK); // Kolor tekstu
        colorButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Color selectedColor = JColorChooser.showDialog(SimplePaintApp.this, "Wybierz kolor", currentColor);
                if (selectedColor != null) {
                    currentColor = selectedColor;
                }
            }
        });

        JLabel brushSizeLabel = new JLabel("Rozmiar pisaka:");
        JSlider brushSizeSlider = new JSlider(1, 10, 5);
        brushSizeSlider.addChangeListener(e -> brushSize = brushSizeSlider.getValue());



        controlPanel.add(colorButton);
        controlPanel.add(brushSizeLabel);
        controlPanel.add(brushSizeSlider);


        add(controlPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        currentColor = Color.BLACK;
        brushSize = 5;
        paths = new ArrayList<>();
        undonePaths = new ArrayList<>();
        pathColors = new ArrayList<>();
        undonePathColors = new ArrayList<>();
        pathSizes = new ArrayList<>();
        undonePathSizes = new ArrayList<>();

        KeyStroke undoKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK);
        KeyStroke redoKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
        canvas.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(undoKeyStroke, "cofnij");
        canvas.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(redoKeyStroke, "odcofnij");
        canvas.getActionMap().put("cofnij", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                undo();
            }
        });
        canvas.getActionMap().put("odcofnij", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                redo();
            }
        });


        saveMenuItem = new JMenuItem("zapis");
        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        saveMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveImage();
            }
        });

        KeyStroke saveKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
        canvas.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(saveKeyStroke, "zapis");
        canvas.getActionMap().put("zapis", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                saveImage();
            }
        });

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Plik");
        JMenuItem saveMenuItem = new JMenuItem("Zapisz");

        undoMenuItem = new JMenuItem("Cofnij");
        undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK));
        undoMenuItem.addActionListener(e -> undo());
        undoMenuItem.setForeground(Color.PINK); //Kolor Cofnij
        redoMenuItem = new JMenuItem("Odcofnij");
        redoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        redoMenuItem.addActionListener(e -> redo());
        redoMenuItem.setForeground(Color.PINK); //Kolor Odcofnij

        fileMenu.add(undoMenuItem);
        fileMenu.add(redoMenuItem);
        menuBar.add(fileMenu);

        setJMenuBar(menuBar);
        saveMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveImage();
            }
        });

        fileMenu.add(saveMenuItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
        fileMenu.add(saveMenuItem);
    }

    private void startPath(int x, int y) {
        List<Point> path = new ArrayList<>();
        path.add(new Point(x, y));
        paths.add(path);
        pathColors.add(currentColor);
        pathSizes.add(brushSize);
        canvas.repaint();
    }

    private void addPointToPath(int x, int y) {
        if (!paths.isEmpty()) {
            List<Point> currentPath = paths.get(paths.size() - 1);
            currentPath.add(new Point(x, y));
            canvas.repaint();
        }
    }

    private void endPath() {
        if (!paths.isEmpty()) {
            List<Point> currentPath = paths.get(paths.size() - 1);
            if (currentPath.size() < 2) {
                // Remove the path if it contains only one point
                paths.remove(paths.size() - 1);
                pathColors.remove(pathColors.size() - 1);
                pathSizes.remove(pathSizes.size() - 1);
            }
            canvas.repaint();
        }
    }

    private void undo() {
        if (!paths.isEmpty()) {
            List<Point> removedPath = paths.remove(paths.size() - 1);
            Color removedPathColor = pathColors.remove(pathColors.size() - 1);
            int removedPathSize = pathSizes.remove(pathSizes.size() - 1);
            undonePaths.add(removedPath);
            undonePathColors.add(removedPathColor);
            undonePathSizes.add(removedPathSize);
            canvas.repaint();
        }
    }

    private void redo() {
        if (!undonePaths.isEmpty()) {
            List<Point> restoredPath = undonePaths.remove(undonePaths.size() - 1);
            Color restoredPathColor = undonePathColors.remove(undonePathColors.size() - 1);
            int restoredPathSize = undonePathSizes.remove(undonePathSizes.size() - 1);
            paths.add(restoredPath);
            pathColors.add(restoredPathColor);
            pathSizes.add(restoredPathSize);
            canvas.repaint();
        }
    }

    private void drawPath(Graphics2D g2d, List<Point> path, Color color, int size) {
        if (path.size() < 2) {
            return;
        }
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(size, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D path2D = new Path2D.Double();
        Point startPoint = path.get(0);
        path2D.moveTo(startPoint.x, startPoint.y);
        for (int i = 1; i < path.size(); i++) {
            Point point = path.get(i);
            Point previousPoint = path.get(i - 1);
            double controlX = (previousPoint.x + point.x) / 2.0;
            double controlY = (previousPoint.y + point.y) / 2.0;
            path2D.quadTo(previousPoint.x, previousPoint.y, controlX, controlY);
        }
        g2d.draw(path2D);
    }

    private void saveImage() {
        BufferedImage image = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        canvas.printAll(g2d);
        g2d.dispose();

        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Zdjecia (*.png, *.jpeg)", "png", "jpeg");
        fileChooser.setFileFilter(filter);
        int option = fileChooser.showSaveDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String extension = ((FileNameExtensionFilter) fileChooser.getFileFilter()).getExtensions()[0];
            file = new File(file.toString() + "." + extension);
            try {
                ImageIO.write(image, extension, file);
                JOptionPane.showMessageDialog(this, "Plik zapisany.", "Sukces", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Nie udalo sie zapisac pliku.", "Blad", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new SimplePaintApp();
            }
        });
    }
}
