import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import jdk.jshell.spi.ExecutionControl;
import net.sf.javabdd.*;

public class QueensLogic13 implements IQueensLogic {

    private int size;		// Size of quadratic game board (i.e. size = #rows = #columns)
    private int[][] board;	// Content of the board. Possible values: 0 (empty), 1 (queen), -1 (no queen allowed)

    private Set<Integer> queenPositions = new HashSet<>(); //the positions where a queen is placed

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
    }

    @Override
    public int[][] getBoard() {
        return board;
    }

    @Override
    public void insertQueen(int column, int row) {
        //if (board[column][row] == -1) //red cross
        if (board[column][row] == 1) { //if queen is placed, remove it
            board[column][row] = 0;
        } else { //if not a queen placed, place it
            board[column][row] = 1;
        }


            // TODO: NOT IMPLEMENTED: LOOK AT RESTRICTIONS??
        
    }

    //RULES: //https://homes.cs.aau.dk/~srba/courses/SV-08/material/09.pdf (exclusion of variables and one queen in each row)
    //excludedRule: no other queen can be positioned on the same row, column or any of the diagonals
    //oneQueenInEachRowRule: at least one queen in each row: conjunction of disjunctions
    //NOTES:
    //  The BDD must be true in the end if the queens are placed correctly
    //  Placing a queen makes the variable true
    //  excludedRule: conjucntion of negations, since placing a queen on one of the excluded positions will make the conjunction false, and hence we do not end up having a true BDD
    //  oneQueenInEachRowRule: disjunction since at least one variable in the disjunction must be true to make the disjunction true, and that is placing a queen in at least one row, since there is a disjunction for each row.
    public void buildRules() {
        int nVars = size*size; // The number of variables to be used is the number of squares on the board
		fact.setVarNum(nVars);

        //rule: no other queen can be positioned on the same row, column or any of the diagonals (https://homes.cs.aau.dk/~srba/courses/SV-08/material/09.pdf)
        for (int variable = 0; variable < nVars; variable++) {
            rules = rules.apply(excludedRule(variable), BDDFactory.and);
        }

        //rule: must be at least one queen in each row (https://homes.cs.aau.dk/~srba/courses/SV-08/material/09.pdf)
        rules = rules.apply(oneQueenInEachRowRule(), BDDFactory.and);
    }

    //https://homes.cs.aau.dk/~srba/courses/SV-08/material/09.pdf
    public BDD oneQueenInEachRowRule() { //disjunction: this means that there must be at least ONE queen in each row to make the BDD true
        BDD rule = True; //this is the overall rule for all the rows
        for (int row = 0; row < size; row++) { //run through the rows and make a rowRule for each
            BDD rowRule = False; //The disjunction identity is false: https://dogedaos.com/wiki/Disjunction.html

            for (int col = 0; col < size; col++) { //run through the cols and make the disjunction of the positions in the row: x1 OR x2 OR x3 etc.
                rowRule.apply(fact.ithVar(getVariable(col, row)), BDDFactory.or);
            }
            rule.apply(rowRule, BDDFactory.and); //conjoin the individual row rules such that row1Rule AND row2Rule AND row3Rule etc.
        }
        return rule;
    }

    //https://homes.cs.aau.dk/~srba/courses/SV-08/material/09.pdf
    public BDD excludedRule(int variable) { //conjunction
        BDD rule = True; //The conjunctive identity is true //https://en.wikipedia.org/wiki/Logical_conjunction
        for (int excludedVariable : getExcludedVariablesBy(variable)) {
            rule.apply(fact.nithVar(excludedVariable), BDDFactory.and); //negate all the excluded variables meaning that the excluded variables must be false to make the conjunction true
        }
        //if the variable is true (the queen is on the position/variable) the excluded variables must be negated
        return fact.ithVar(variable).imp(rule);
    }

    //help get methods: lists
    public ArrayList<Integer> getExcludedVariablesBy(int variable) {

        ArrayList<Integer> excludedVariables = getVariablesInSameCol(variable); //exclude variables in same col
        excludedVariables.addAll(getVariablesInSameRow(variable)); //exclude variables in same col
        excludedVariables.addAll(getVariablesDiagonal(variable)); //exclude variables in same dia

        return excludedVariables;
    }

    public ArrayList<Integer> getVariablesInSameRow(int variable) {
        ArrayList<Integer> variablesInSameRow = new ArrayList<>();
    
        for (int col=0; col<size; col++) {
            
            int row = getRowFromCol(variable, col);
            int variableInRow = getVariable(col, row);

            if (variableInRow != variable) {
                variablesInSameRow.add(variableInRow);
            }
        }
        return variablesInSameRow;
    }

    public ArrayList<Integer> getVariablesInSameCol(int variable) {
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

    public ArrayList<Integer> getVariablesDiagonal(int variable) {
        ArrayList<Integer> variablesInSameDia = new ArrayList<>();
        int[] leftUp = {-1,-1};
        int[] leftDown = {-1,1};
        int[] rightUp = {1,-1};
        int[] rightDown = {1,1};

        int[][] directions = {leftUp, leftDown, rightDown, rightUp};

        for (int[] direction : directions) {
            int col =  getCol(variable) + direction[0];
            int row = getRow(variable) + direction[1];

            while (isOnBoard(col,row)) {
                variablesInSameDia.add(row * size + col);

                //go further in the direction:
                col += direction[0];
                row += direction[1];
            }
        }
        return  variablesInSameDia;
    }

    //help get methods: variable = col + row * size
    public int getVariable(int col, int row) {
        return col + row*size;
    }

    public int getRowFromCol(int variable, int col) {
        return (variable-col)/size;
    }

    public int getRow(int variable) {
        //return variable/size; //I don't know if this gives rows. It depends on how divid works on ints, I think...
        return getRowFromCol(variable, getCol(variable));
    }

    public int getCol(int variable) {
        return variable % size;
    }

    // help bool
    public boolean isOnBoard(int col, int row) {
        return row >= 0 && row < size && col >= 0 && col < size;
    }

    //RESTRICTIONS
    //returns BDD for a given variable. It is the restrictions for that given variable??
    public BDD getRestrictions(int variable) {
        return fact.ithVar(variable);
    }
}
