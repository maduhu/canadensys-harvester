package net.canadensys.processing.occurrence.processor;

import java.math.BigInteger;
import java.util.Map;

import net.canadensys.dataportal.occurrence.model.OccurrenceRawModel;
import net.canadensys.processing.ItemProcessorIF;
import net.canadensys.processing.exception.TaskExecutionException;
import net.canadensys.processing.occurrence.SharedParameterEnum;

import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Processing each line read from a Darwin Core Archive.
 * Attribute a unique id to link the raw and processed model together
 * @author canadenys
 *
 */
@Component("lineProcessor")
public class DwcaLineProcessor implements ItemProcessorIF<OccurrenceRawModel, OccurrenceRawModel>{

	@Autowired
	@Qualifier(value="bufferSessionFactory")
	private SessionFactory sessionFactory;
	
	private StatelessSession session;
	private SQLQuery sqlQuery;
	
	//get log4j handler
	private static final Logger LOGGER = Logger.getLogger(DwcaLineProcessor.class);
	
	@Override
	public void init(){
		session = sessionFactory.openStatelessSession();
		session.beginTransaction();
		sqlQuery = session.createSQLQuery("SELECT nextval('buffer.occurrence_raw_auto_id_seq')");
	}
	
	@Override
	public void destroy(){
		session.getTransaction().commit();
	}
	
	/**
	 * @return same instance of OccurrenceRawModel with modified values
	 */
	@Override
	public OccurrenceRawModel process(OccurrenceRawModel occModel, Map<SharedParameterEnum,Object> sharedParameters) {
		//TODO could be done at init phase?
		String datasetShortname = (String)sharedParameters.get(SharedParameterEnum.DATASET_SHORTNAME);
        
        if(datasetShortname == null){
			LOGGER.fatal("Misconfigured processor : needs  sourceFileId");
			throw new TaskExecutionException("Misconfigured processor");
		}
		occModel.setSourcefileid(datasetShortname);
		
		BigInteger auto_id = (BigInteger)sqlQuery.uniqueResult();
		occModel.setAuto_id(auto_id.intValue());
		
		//TODO maybe check the uniqueness of occModel.getId()
		return occModel;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

}
