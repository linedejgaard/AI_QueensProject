import java.util.ArrayList;
import net.sf.javabdd.*;

public class QueensLogic13 implements IQueensLogic {

    private int size;		// Size of quadratic game board (i.e. size = #rows = #columns)
    private int[][] board;	// Content of the board. Possible values: 0 (empty), 1 (queen), -1 (no queen allowed)

    int numberOfNodes = 2000000; //suggest by the project description
    int cache = 200000; //suggest by the project description

    BDDFactory fact = JFactory.init(numberOfNodes,cache);
    BDD True = fact.one();
    BDD False = fact.zero();

    BDD rules = True; //this is going to be a conjunction, and since the identity of conjunction is true, this is set to true
    
    @Override
    public void initializeBoard(int size) {
        this.size = size;
        this.board = new int[size][size];
        buildRules();
        updateBoard();
    }

    @Override
    public int[][] getBoard() {
        return board;
    }

    @Override
    public void insertQueen(int column, int row) {
        //-1: red cross
        //1: if queen is placed
        if (board[column][row] == 0) { //if not a queen placed, place it
            board[column][row] = 1; //place the queen
            int variable = getVariable(column,row);
            updateRestrictions(variable);
            updateBoard();
        }
    }

    public void updateBoard() {
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (board[col][row] == 0) {
                    BDD pos = rules.restrict(fact.ithVar(getVariable(col, row)));
                    BDD neg = rules.restrict(fact.nithVar(getVariable(col, row)));
                    if (neg.isZero()) { //not inserting a queen here is a contradiction
                        board[col][row] = 1;
                        insertQueen(col, row);
                    } else if (pos.isZero()) { //inserting a queen here is a contradiction
                        board[col][row] = -1;
                    }
                }
            }
        }
    }

    public void printBoard() {
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                System.out.print(board[col][row] + " ");
            }
            System.out.println();
        }
    }

    //RULES: //https://learnit.itu.dk/pluginfile.php/312490/mod_label/intro/a97.pdf (exclusion of variables and one queen in each row)
    //excludedRule: no other queen can be positioned on the same row, column or any of the diagonals
    //oneQueenInEachRowRule: at least one queen in each row: conjunction of disjunctions
    //NOTES:
    //  The BDD must be true in the end if the queens are placed correctly
    //  Placing a queen makes the variable true
    //  excludedRule: conjucntion of negations, since placing a queen on one of the excluded positions will make the conjunction false, and hence we do not end up having a true BDD
    //  oneQueenInEachRowRule: disjunction since at least one variable in the disjunction must be true to make the disjunction true, and that is placing a queen in at least one row, since there is a disjunction for each row.
    public void buildRules() { //INIT RULES
        int nVars = size*size; // The number of variables to be used is the number of squares on the board
		fact.setVarNum(nVars);

        //rule: no other queen can be positioned on the same row, column or any of the diagonals (https://learnit.itu.dk/pluginfile.php/312490/mod_label/intro/a97.pdf)
        for (int variable = 0; variable < nVars; variable++) {
            rules = rules.and(excludedRule(variable));
        }

        //rule: must be at least one queen in each row (https://learnit.itu.dk/pluginfile.php/312490/mod_label/intro/a97.pdf)
        rules = rules.and(oneQueenInEachRowRule());
    }

    //https://learnit.itu.dk/pluginfile.php/312490/mod_label/intro/a97.pdf
    public BDD oneQueenInEachRowRule() { //disjunction: this means that there must be at least ONE queen in each row to make the BDD true
        BDD rule = True; //this is the overall rule for all the rows
        for (int row = 0; row < size; row++) { //run through the rows and make a rowRule for each
            BDD rowRule = False; //The disjunction identity is false: https://dogedaos.com/wiki/Disjunction.html

            for (int col = 0; col < size; col++) { //run through the cols and make the disjunction of the positions in the row: x1 OR x2 OR x3 etc.
                rowRule = rowRule.or(fact.ithVar(getVariable(col, row)));
            }
            rule = rule.and(rowRule); 
        }
        return rule;
    }

    //https://learnit.itu.dk/pluginfile.php/312490/mod_label/intro/a97.pdf
    public BDD excludedRule(int variable) { //conjunction
        BDD rule = True; //The conjunctive identity is true //https://en.wikipedia.org/wiki/Logical_conjunction
        for (int excludedVariable : getExcludedVariablesBy(variable)) {
            rule = rule.and(fact.nithVar(excludedVariable)); 
        }
        //if the variable is true (the queen is on the position/variable) the excluded variables must be negated
        return fact.ithVar(variable).imp(rule);
    }

    //help get methods: lists
    private ArrayList<Integer> getExcludedVariablesBy(int variable) {
        ArrayList<Integer> excludedVariables = getVariablesInSameCol(variable); //exclude variables in same col
        excludedVariables.addAll(getVariablesInSameRow(variable)); //exclude variables in same row
        excludedVariables.addAll(getVariablesDiagonal(variable)); //exclude variables in same dia

        return excludedVariables;
    }

    private ArrayList<Integer> getVariablesInSameRow(int variable) {
        ArrayList<Integer> variablesInSameRow = new ArrayList<>();

        int row = getRow(variable);
    
        for (int col=0; col<size; col++) {

            int variableInRow = getVariable(col, row);

            if (variableInRow != variable) {
                variablesInSameRow.add(variableInRow);
            }
        }
        return variablesInSameRow;
    }

    private ArrayList<Integer> getVariablesInSameCol(int variable) {
        ArrayList<Integer> variablesInSameCol = new ArrayList<>();
        int col = getCol(variable);

        for (int row = 0; row < size; row++) {
            int variableInCol = getVariable(col, row);

            if (variableInCol != variable) {
                variablesInSameCol.add(variableInCol);
            }
        }
        return variablesInSameCol;
    }

    private ArrayList<Integer> getVariablesDiagonal(int variable) {
        ArrayList<Integer> variablesInSameDia = new ArrayList<>();
        int[] leftUp = {-1,-1};
        int[] leftDown = {-1,1};
        int[] rightUp = {1,-1};
        int[] rightDown = {1,1};

        int[][] directions = {leftUp, leftDown, rightDown, rightUp};

        for (int[] direction : directions) {
            int newCol =  getCol(variable) + direction[0];
            int newRow = getRow(variable) + direction[1];

            while (isOnBoard(newCol,newRow)) {
                variablesInSameDia.add(getVariable(newCol,newRow));

                //go further in the direction:
                newCol += direction[0];
                newRow += direction[1];
            }
        }
        return  variablesInSameDia;
    }

    //help get methods: variable = col + row * size
    private int getVariable(int col, int row) {
        return col + row*size;
    }

    private int getRowFromCol(int variable, int col) {
        return (variable-col)/size;
    }

    private int getRow(int variable) {
        return getRowFromCol(variable, getCol(variable));
    }

    private int getCol(int variable) {
        return variable % size;
    }

    // help bool
    private boolean isOnBoard(int col, int row) {
        return row >= 0 && row < size && col >= 0 && col < size;
    }

    //updates the restrictions, when a queen is added or removed
    //method is called every time a queen is added or removed
    private void updateRestrictions(int variable) {
        rules = rules.restrict(fact.ithVar(variable));
    }
}
