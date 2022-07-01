package com.company;

import java.awt.*; //!Рисование окна
import java.awt.event.*; //!Обработка событий
import javax.swing.*; //!Рисует стандартные объекты в окне
import java.util.*;


public class Main extends JFrame {

    final int[] colorNumbers = {0x0000FF, 0x008000, 0xFF0000, 0x800000, 0x0};
    final int fieldSize = 16;
    final int windowPosition_Y = 280;
    final int windowPosition_X = 720;
    final int sizeBlock = 30;
    final int coordinate_X = 16;
    final int coordinate_Y = 65;
    final int leftMouseBottom = 1;
    final int rightMouseBottom = 3;
    final int globalCountMines = 40;
    int position_mine_X, position_mine_Y;
    int count;
    final String title = "Minesweeper";
    boolean win, defeat;
    Random random = new Random();
    Cell[][] field = new Cell[fieldSize][fieldSize];
    Image image = new ImageIcon("src/080091.png").getImage();
    JButton button = new JButton("Начать заново");

    Main() {
        TestActionListener testActionListener = new TestActionListener();
        button.addActionListener(testActionListener);
        setTitle(title); //? Заголовок окна
        setDefaultCloseOperation(EXIT_ON_CLOSE); //? При нажатии кнопки "Х" закроется программа
        setBounds(windowPosition_X, windowPosition_Y, fieldSize * sizeBlock + coordinate_X, fieldSize *
                sizeBlock + coordinate_Y); //? Стартовое положение окна и его размер
        setResizable(false); //? Возможность масштабировать окно
        Linen linen = new Linen(); //? Объект класса
        linen.setBackground(Color.white); //? Задаем заднему фону белый цвет
        mouseClick(linen); //? Вызов функции обработки мыши
        add(BorderLayout.CENTER, linen); //? Окно ставиться в центр экрана
        add(BorderLayout.SOUTH, button); //? Добавление кнопки внизу окна
        setVisible(true); //? Делаем окно видимым
        fillField(); //?Генерация поля
    } //!Конструктор

    public void mouseClick(Linen linen) {
        linen.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) { //? Обработка нажатии кнопки мыши
                super.mouseReleased(e); //? Вызываем метод у родительского класса
                int x = e.getX() / sizeBlock;
                int y = e.getY() / sizeBlock;

                if (e.getButton() == leftMouseBottom && !defeat && !win) { //?Проверка на нажатие левой кнопки мыши
                    if (field[y][x].isNotOpen()) {
                        clickOpenCell(x, y);
                        win = count == fieldSize * fieldSize - globalCountMines; //?Проверка на победу, после открытия ячейки
                        if (defeat) {
                            position_mine_X = x;
                            position_mine_Y = y;
                        }
                    }
                }

                if (e.getButton() == rightMouseBottom) { //?Проверка на нажатие правой кнопки мыши
                    field[y][x].setFlag();
                }
                linen.repaint(); //? Перерисовка окна
            }
        });
    }

    void clickOpenCell(int x, int y) {
        if (x < 0 || x > fieldSize - 1 || y < 0 || y > fieldSize - 1) {
            return;
        }
        if (!field[y][x].isNotOpen()) {
            return;
        }
        field[y][x].open();
        if (field[y][x].getCountBomb() > 0 || defeat) {
            return;
        }
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                clickOpenCell(x + i, y + j);
            }
        }
    }

    void fillField() {
        int i, j;
        int countMines = 0;

        for (i = 0; i < fieldSize; i++) {
            for (j = 0; j < fieldSize; j++) { //? Создание клеток (в каждую клетку попадает объект Cell)
                field[j][i] = new Cell();
            }
        }

        while (countMines < globalCountMines) {
            do {
                i = random.nextInt(fieldSize);
                j = random.nextInt(fieldSize); //? Генератор радномных мин
            } while (field[j][i].isMined());
            field[j][i].setMine();
            countMines++;
        }

        for (i = 0; i < fieldSize; i++) { //? Подсчет соседних бомб
            for (j = 0; j < fieldSize; j++) {
                if (!field[j][i].isMined()) {
                    int countNum = 0;
                   for (int dx = -1; dx < 2; dx++) {
                        for (int dy = -1; dy < 2; dy++) {
                            int pX = i + dx;
                            int pY = j + dy;
                            if (pX < 0 || pY < 0 || pX > fieldSize -1 || pY > fieldSize - 1) {
                                pX = i;
                                pY = j;
                            }
                            countNum += (field[pY][pX].isMined()) ? 1 : 0;
                        }
                        field[j][i].setCountBomb(countNum);
                    }
                }
            }
        }
    }

    class Cell{
        private boolean isOpen;
        private boolean isMine;
        private boolean isFlag;
        private int countNearBombs;

        void open() {
            isOpen = true;
            defeat = isMine;
            if (!isMine) {
                count++;
            }
        }

        public void paint(Graphics g, int x, int y) {
            g.setColor(Color.lightGray);
            g.drawRect(x * sizeBlock, y * sizeBlock, sizeBlock, sizeBlock);
            //? Рисование серых квадратов

            if (!isOpen) {
                if ((defeat || win ) && isMine) { //? Проверка если я победил или проиграл, и в то же время ячейка заминирована
                    drawBomb(g, x, y, Color.black); //? Рисовка бомбы
                } else {
                    g.fill3DRect(x * sizeBlock, y * sizeBlock, sizeBlock, sizeBlock, true);
                    if (isFlag) {
                        paintFlag(g, x, y); //? Рисование флага
                    } else {
                        g.setColor(Color.lightGray);
                        g.fill3DRect(x * sizeBlock, y * sizeBlock, sizeBlock, sizeBlock, true); //? Рисуем серый квадрат, если ячейка закрыта
                    }
                }
            } else {
                if (isMine) { //? Если в ячейке есть бомба
                    drawBomb(g, x, y, defeat? Color.red : Color.black); //Рисовка бомбы
                } else { //? Если в ячейке бомбы нет
                    if (countNearBombs > 0) {
                        paintString(g, Integer.toString(countNearBombs), x, y, new Color(colorNumbers[countNearBombs - 1])); //? Рисуеться цифра о количестве бомб рядом
                    }
                }
            }
        }

        void paintFlag(Graphics g, int x, int y) {
            g.fill3DRect(x * sizeBlock, y * sizeBlock, sizeBlock, sizeBlock, true);
            g.drawImage(image, x * sizeBlock, y * sizeBlock, null,null);
        }

        void paintString(Graphics g, String str, int x, int y, Color color) { //!Рисование цифр на ячейках
            g.setColor(color);
            g.setFont(new Font("", Font.BOLD, sizeBlock));
            g.drawString(str, x * sizeBlock + 8, y * sizeBlock + 26);
        }

        void drawBomb(Graphics g, int x, int y, Color color) { //!Рисование бомбы
            g.setColor(color);
            g.fillRect(x * sizeBlock + 7, y * sizeBlock + 10, 18, 10);
            g.fillRect(x * sizeBlock + 11, y * sizeBlock + 6, 10, 18);
            g.fillRect(x * sizeBlock + 9, y * sizeBlock + 8, 14, 14);
            g.setColor(Color.white);
            g.fillRect(x * sizeBlock + 11, y * sizeBlock + 10, 4, 4);
        }

        public void setMine() {
            isMine = true;
        }

        public void setCountBomb(int countBomb) {
            countNearBombs = countBomb;
        }

        public int getCountBomb() {
            return countNearBombs;
        }

        public void setFlag() {
            isFlag = !isFlag;
        }

        public boolean isMined() { //!Проверка на заминирована ли ячейка
            return isMine;
        }

        public boolean isNotOpen() {
            return !isOpen;
        }

    }

    class Linen extends JPanel{
        public void paint(Graphics g) {
            super.paint(g); //? Родительский метод отрисовки
            for (int x = 0; x < fieldSize; x++) {
                for (int y = 0; y < fieldSize; y++) {
                    field[y][x].paint(g, x, y);
                }
            }
        }
    }

    class TestActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            if (event.getSource() == button) {
                win = false;
                defeat = false;
                setTitle(title); //? Заголовок окна
                setDefaultCloseOperation(EXIT_ON_CLOSE); //? При нажатии кнопки "Х" закроется программа
                setBounds(windowPosition_X, windowPosition_Y, fieldSize * sizeBlock + coordinate_X, fieldSize *
                        sizeBlock + coordinate_Y); //? Стартовое положение окна и его размер
                setResizable(false); //? Запрет пользователю изменять размер кадра
                Linen linen = new Linen(); //? Объект класса
                linen.setBackground(Color.white); //? Задаем заднему фону белый цвет
                mouseClick(linen); //? Вызов функции обработки мыши
                add(BorderLayout.CENTER, linen); //? Окно ставиться в центр экрана
                add(BorderLayout.SOUTH, button); //? Добавление кнопки внизу окна
                setVisible(true); //? Делаем окно видимым
                fillField(); //?Генерация поля
            }
        }
    }

    public static void main(String[] args) {
            new Main();
    }
}
