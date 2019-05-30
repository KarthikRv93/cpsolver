package org.cpsolver.instructor.constraints;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.cpsolver.instructor.model.Attribute;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.model.GlobalConstraint;
import org.cpsolver.ifs.util.ToolBox;
import org.cpsolver.instructor.model.Instructor.Context;
import org.cpsolver.instructor.model.InstructorSchedulingModel;
import org.cpsolver.instructor.model.Preference;
import org.cpsolver.instructor.model.TeachingAssignment;
import org.cpsolver.instructor.model.TeachingRequest;

/**
 * Instructor Constraint. This is the main constraint of the problem, ensuring
 * that an instructor gets a consistent list of assignments. It checks for
 * instructor availability, maximal load, time conflicts, and whether the given
 * assignments are of the same course (if desired).
 * 
 * @version IFS 1.3 (Instructor Sectioning)<br>
 *          Copyright (C) 2016 Tomas Muller<br>
 *          <a href="mailto:muller@unitime.org">muller@unitime.org</a><br>
 *          <a href="http://muller.unitime.org">http://muller.unitime.org</a>
 *          <br>
 *          <br>
 *          This library is free software; you can redistribute it and/or modify
 *          it under the terms of the GNU Lesser General Public License as
 *          published by the Free Software Foundation; either version 3 of the
 *          License, or (at your option) any later version. <br>
 *          <br>
 *          This library is distributed in the hope that it will be useful, but
 *          WITHOUT ANY WARRANTY; without even the implied warranty of
 *          MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *          Lesser General Public License for more details. <br>
 *          <br>
 *          You should have received a copy of the GNU Lesser General Public
 *          License along with this library; if not see
 *          <a href='http://www.gnu.org/licenses/'>http://www.gnu.org/licenses/
 *          </a>.
 */
public class InstructorConstraint extends GlobalConstraint<TeachingRequest.Variable, TeachingAssignment> {
    private static org.apache.log4j.Logger sLogger = org.apache.log4j.Logger.getLogger(InstructorConstraint.class);

    /**
     * Constructor
     */
    public InstructorConstraint() {
    }

    @Override
    public void computeConflicts(Assignment<TeachingRequest.Variable, TeachingAssignment> assignment,
            TeachingAssignment value, Set<TeachingAssignment> conflicts) {
        Context context = value.getInstructor().getContext(assignment);

        // sLogger.info((Attribute)value.variable().getRequest().getAttributePreferences().get(0).getTarget());
        /*
         * for(Preference<Attribute> p:
         * value.variable().getRequest().getAttributePreferences()){
         * if(value.variable().getRequest().onlineCourse()){
         * sLogger.info(p.getTarget().toString()+value.variable().getRequest().
         * getCourse().getCourseName()); } }
         */
        for (Attribute p : value.getInstructor().getAttributes()) {
            if (p.getType().getTypeName().equals("onlinePref")) {
                //sLogger.info(" instructor online : "+ context.getInstructor().getName());
                value.getInstructor().setOnline(true);
            } else if (value.getInstructor().isOnline() && p.getType().getTypeName().equals("onlineCourses")) {
                value.getInstructor().setNumOnlineCourses(Integer.parseInt(p.getAttributeName()));
            }
            //sLogger.info(" is online: " + value.getInstructor().isOnline() + "type : "+p.getType().getTypeName());
        }
        // Check availability
        if (context.getInstructor().getTimePreference(value.variable().getRequest()).isProhibited()) {
            conflicts.add(value);       
            return;
        }

        // Check for overlaps
        for (TeachingAssignment ta : context.getAssignments()) {
            if (ta.variable().equals(value.variable()) || conflicts.contains(ta))
                continue;

            if (ta.variable().getRequest().overlaps(value.variable().getRequest()))
                conflicts.add(ta);
        }

        // Same course and/or common
        for (TeachingAssignment ta : context.getAssignments()) {
            if (ta.variable().equals(value.variable()) || conflicts.contains(ta))
                continue;
            if (ta.variable().getRequest().isSameCourseViolated(value.variable().getRequest())
                    || ta.variable().getRequest().isSameCommonViolated(value.variable().getRequest()))
                conflicts.add(ta);
        }

        int count = 0;
        for (TeachingAssignment ta : context.getAssignments()) {
            if ((!value.variable().getRequest().onlineCourse())
                    || (context.getInstructor().getExternalId().equals("10"))){
                //sLogger.info("break");
                break;
            }
            if(!context.getInstructor().isOnline()){
                sLogger.info(" not online: "+ context.getInstructor().getName() + value.variable().getRequest().getCourse().getCourseName());
                conflicts.add(value);
                break;
            }
            if (ta.variable().getRequest().onlineCourse()) {
                sLogger.info(" online course : "+ context.getInstructor().isOnline() + " number of OLC : "+ context.getInstructor().getNumOnlineCourses());
                sLogger.info("count : " + count );
                if (count >=(context.getInstructor().getNumOnlineCourses()-1)) {
                    conflicts.add(ta);
                    break;
                }
                count++;
            }
        }

        /*
         * int count = 0; for (TeachingAssignment ta :
         * context.getAssignments()){ if
         * ((!ta.variable().equals(value.variable())) ||
         * (!conflicts.contains(ta))) { if
         * ((!value.variable().getRequest().onlineCourse()) ||
         * (context.getInstructor().getExternalId().equals("10"))) break; if
         * (ta.variable().getRequest().onlineCourse()) { if (count >=2) {
         * conflicts.add(ta); } count++; } } }
         */

        // Check load
        float load = value.variable().getRequest().getLoad();
        List<TeachingAssignment> adepts = new ArrayList<TeachingAssignment>();
        for (TeachingAssignment ta : context.getAssignments()) {
            if (ta.variable().equals(value.variable()) || conflicts.contains(ta))
                continue;

            adepts.add(ta);
            load += ta.variable().getRequest().getLoad();
        }
        while (load > context.getInstructor().getMaxLoad()) {
            if (adepts.isEmpty()) {
                conflicts.add(value);
                break;
            }
            TeachingAssignment conflict = ToolBox.random(adepts);
            load -= conflict.variable().getRequest().getLoad();
            adepts.remove(conflict);
            conflicts.add(conflict);
        }
    }

    @Override
    public String getName() {
        return "Instructor Constraint";
    }

    @Override
    public String toString() {
        return getName();
    }
}
