package job;

import java.util.Map;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.context.ApplicationContext;

import bean.MoofeelSign;

public class MoofeelSignJob implements StatefulJob{


	@Override
	public void execute(JobExecutionContext jctx) throws JobExecutionException {
		// TODO Auto-generated method stub
		Map dataMap = jctx.getJobDetail().getJobDataMap();
		ApplicationContext ctx = (ApplicationContext) dataMap.get("applicationContext");
		MoofeelSign moofeel =  (MoofeelSign) ctx.getBean("moofeelSign");
		try {
			moofeel.sign();
//			moofeel.test();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
