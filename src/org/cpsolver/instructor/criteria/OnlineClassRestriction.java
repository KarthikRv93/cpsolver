package org.cpsolver.instructor.criteria;

import java.util.Collection; 
import java.util.Set;
import org.apache.log4j.Logger;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.instructor.model.Instructor;
import org.cpsolver.instructor.model.InstructorSchedulingModel;
import org.cpsolver.instructor.model.TeachingAssignment;
import org.cpsolver.instructor.model.TeachingRequest;

public class OnlineClassRestriction extends InstructorSchedulingCriterion {
    private static Logger sLog = Logger.getLogger(OnlineClassRestriction.class);
    
    
    public double getWeightDefault(DataProperties config) {
        return 100.0;
    }

    @Override
    public double getValue(Assignment<TeachingRequest.Variable, TeachingAssignment> assignment, TeachingAssignment value, Set<TeachingAssignment> conflicts) {
        Instructor.Context context = value.getInstructor().getContext(assignment);
        double penalty = 0.0;
        int pairs = 0;
        if(!value.variable().getRequest().onlineCourse()){
            sLog.info(value.variable().getRequest().getCourse().getCourseName().contains("ONLINE")+ "return0");
            return 0;
        }
        int onlineLoad = 2;
        for (TeachingAssignment ta : context.getAssignments()) {
            if(ta.variable().getRequest().getCourse().getCourseName().contains("ONLINE")){
                if(value.getInstructor().getExternalId().equals("10"))
                    onlineLoad = 10;
                sLog.info(ta.variable().getRequest().getCourse().getCourseName());
                sLog.info("isOnline or not"+ta.variable().getRequest().getCourse().getCourseName().contains("ONLINE"));
                sLog.info("penalty"+(pairs < onlineLoad ? 0.0 : 1000));
                pairs++;
            }
        }
        return (pairs < onlineLoad ? 0.0 : 1000);
    } 
    
    @Override
    public String getAbbreviation() {
        return "OnlineClassRestriction";
    }
    
    @Override
    public String getName() {
        return "Online Class Restriction";
    }
    
}
