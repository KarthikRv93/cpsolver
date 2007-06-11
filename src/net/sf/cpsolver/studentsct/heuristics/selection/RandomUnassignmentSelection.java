package net.sf.cpsolver.studentsct.heuristics.selection;

import java.util.Enumeration;
import java.util.Vector;

import net.sf.cpsolver.ifs.heuristics.NeighbourSelection;
import net.sf.cpsolver.ifs.model.Neighbour;
import net.sf.cpsolver.ifs.solution.Solution;
import net.sf.cpsolver.ifs.solver.Solver;
import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.ifs.util.ToolBox;
import net.sf.cpsolver.studentsct.StudentSectioningModel;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Student;

/**
 * Random unassignment of some (randomly selected) students. 
 * 
 * <br><br>
 * In each step a student is randomly selected with the given probabilty.
 * Null is returned otherwise (controll is passed to the following {@link NeighbourSelection}).
 *
 * <br><br>
 * Parameters:
 * <br>
 * <table border='1'><tr><th>Parameter</th><th>Type</th><th>Comment</th></tr>
 * <tr><td>Neighbour.RandomUnassignmentProb</td><td>{@link Double}</td><td>Probability of a random selection of a student.</td></tr>
 * </table>
 * <br><br>
 * 
 * @version
 * StudentSct 1.1 (Student Sectioning)<br>
 * Copyright (C) 2007 Tomas Muller<br>
 * <a href="mailto:muller@ktiml.mff.cuni.cz">muller@ktiml.mff.cuni.cz</a><br>
 * Lazenska 391, 76314 Zlin, Czech Republic<br>
 * <br>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <br><br>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * <br><br>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
public class RandomUnassignmentSelection implements NeighbourSelection {
    private Vector iStudents = null;
    protected double iRandom = 0.5;
    
    /** 
     * Constructor
     * @param properties configuration
     */
    public RandomUnassignmentSelection(DataProperties properties) {
        iRandom = properties.getPropertyDouble("Neighbour.RandomUnassignmentProb", iRandom);
    }
    
    /**
     * Initialization
     */
    public void init(Solver solver) {
        iStudents = ((StudentSectioningModel)solver.currentSolution().getModel()).getStudents();
    }
    
    /**
     * With the given probabilty, a student is randomly selected to be unassigned.
     * Null is returned otherwise.
     */
    public Neighbour selectNeighbour(Solution solution) {
        if (Math.random()<iRandom) {
            Student student = (Student)ToolBox.random(iStudents);
            return new UnassignStudentNeighbour(student);
        }
        return null;
    }
    
    /** Unassignment of all requests of a student */
    public static class UnassignStudentNeighbour extends Neighbour {
        private Student iStudent = null;
        
        /**
         * Constructor
         * @param student a student to be unassigned 
         */
        public UnassignStudentNeighbour(Student student) {
            iStudent = student;
        }
        
        /** All requests of the given student are unassigned */
        public void assign(long iteration) {
            for (Enumeration e=iStudent.getRequests().elements();e.hasMoreElements();) {
                Request request = (Request)e.nextElement();
                if (request.getAssignment()!=null)
                    request.unassign(iteration);
            }
        }
        
        public String toString() {
            StringBuffer sb = new StringBuffer("Un{");
            sb.append(" "+iStudent);
            sb.append(" }");
            return sb.toString();
        }
        
    }
    
}