package de.kostari.cloud.core.utils.render;

/**
 * Splits one loaded texture into equally sized, lightweight texture regions.
 *
 * Cell coordinates start at the top-left of the image. Cells share the sheet's
 * OpenGL texture, so requesting frames does not allocate or upload new textures.
 */
public class TextureSheet {

    private final String filePath;
    private final int cellWidth;
    private final int cellHeight;
    private final int columnCount;
    private final int rowCount;
    private final Texture sheetTexture;
    private final Texture[] cells;

    public TextureSheet(String filePath, int cellWidth, int cellHeight) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("Texture sheet file path cannot be empty");
        }
        if (cellWidth <= 0 || cellHeight <= 0) {
            throw new IllegalArgumentException("Texture sheet cell size must be greater than zero");
        }

        this.filePath = filePath;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.sheetTexture = new Texture(filePath).load();

        int sheetWidth = (int) sheetTexture.getWidth();
        int sheetHeight = (int) sheetTexture.getHeight();
        if (sheetWidth % cellWidth != 0 || sheetHeight % cellHeight != 0) {
            throw new IllegalArgumentException(
                    "Texture sheet size " + sheetWidth + "x" + sheetHeight
                            + " is not divisible by cell size " + cellWidth + "x" + cellHeight);
        }

        this.columnCount = sheetWidth / cellWidth;
        this.rowCount = sheetHeight / cellHeight;
        this.cells = new Texture[columnCount * rowCount];

        for (int y = 0; y < rowCount; y++) {
            for (int x = 0; x < columnCount; x++) {
                cells[indexOf(x, y)] = Texture.region(
                        sheetTexture,
                        x * cellWidth,
                        y * cellHeight,
                        cellWidth,
                        cellHeight);
            }
        }
    }

    public Texture getCellTexture(int cellX, int cellY) {
        return cells[indexOf(cellX, cellY)];
    }

    /**
     * Returns a cell by row-major index.
     */
    public Texture getCellTexture(int index) {
        if (index < 0 || index >= cells.length) {
            throw new IndexOutOfBoundsException(
                    "Texture sheet cell index " + index + " is outside 0.." + (cells.length - 1));
        }
        return cells[index];
    }

    /**
     * Returns all cells in a row as a new array.
     */
    public Texture[] getRow(int row) {
        if (row < 0 || row >= rowCount) {
            throw new IndexOutOfBoundsException(
                    "Texture sheet row " + row + " is outside 0.." + (rowCount - 1));
        }

        Texture[] result = new Texture[columnCount];
        System.arraycopy(cells, row * columnCount, result, 0, columnCount);
        return result;
    }

    /**
     * Returns all cells in row-major order as a new array.
     */
    public Texture[] getCells() {
        return cells.clone();
    }

    public String getFilePath() {
        return filePath;
    }

    public int getCellWidth() {
        return cellWidth;
    }

    public int getCellHeight() {
        return cellHeight;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getCellCount() {
        return cells.length;
    }

    public Texture getSheetTexture() {
        return sheetTexture;
    }

    private int indexOf(int cellX, int cellY) {
        if (cellX < 0 || cellX >= columnCount || cellY < 0 || cellY >= rowCount) {
            throw new IndexOutOfBoundsException(
                    "Texture sheet cell (" + cellX + ", " + cellY + ") is outside "
                            + columnCount + "x" + rowCount);
        }
        return cellY * columnCount + cellX;
    }
}
