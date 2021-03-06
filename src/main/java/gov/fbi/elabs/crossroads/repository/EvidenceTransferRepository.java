package gov.fbi.elabs.crossroads.repository;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import gov.fbi.elabs.crossroads.exception.BaseApplicationException;

@Repository
@SuppressWarnings("unchecked")
public class EvidenceTransferRepository extends BaseRepository {

	private static Logger logger = LoggerFactory.getLogger(EvidenceTransferRepository.class);
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

	public String setQueryForEvidenceTransferTable(Integer batchID, String evidenceTransferTypeCode, Integer employeeID,
			String loggedinUser, String comments, Integer transferReason, Integer storageAreaID,
			String storageLocationID, Integer locationID, Integer organizationID, Integer witness1ID,
			Integer witness2ID, Integer newBatchId) {
		Date date = new Date();
		String todaysDate = dateFormat.format(date);
		System.out.println("DATE : " + todaysDate);
		StringBuilder sql = new StringBuilder("Insert into EvidenceTransfer "
				+ " (TransferDate, BatchID ,BatchCreationDate ,EvidenceTransferTypeCode ,EvidenceTransferStatusCode ,"
				+ " FromEmployeeID ,ToEmployeeID ,FromLocationID ,ToLocationID ,FromOrganizationID ,ToOrganizationID ,"
				+ " LockboxTransferID ,StorageAreaID ,StorageLocationCode ,"
				+ " VerifiedByID  ,FSLabNum  ,SubmissionNum  ,EvidenceType  ,"
				+ " EvidenceID  ,Comments ,BatchComments  ,EvidenceTransferReasonID  ,"
				+ " CreatedBy  ,CreatedDate ,LastModifiedBy  ,LastModifiedDate ,"
				+ "IsActive  ,Witness1Id  ,Witness2Id  ,AgencyLocationId  ,OfficerId    ) ");
		sql.append("  select " + "convert(DATETIME," + "\'" + todaysDate + "\'" + ",20)" + " , " + newBatchId + " , "
				+ "convert(DATETIME," + "\'" + todaysDate + "\'" + ",20)" + ", " + "\'" + "RS" + "\'" + " as TT "
				+ ",'C'," + "null," + employeeID + "," + "e.CustodyLocationID," + "\'" + locationID + "\'"
				+ ",e.CustodyOrganizationID," + "\'" + organizationID + "\'" + "," + "null," + "null" + "," + "null"
				+ ",null," + "e.FSLabNum, e.CurrentSubmissionNum,e.EvidenceType,e.EvidenceID,"
				+ (comments != null ? ("\'" + comments + "\'") : null) + ",null," + transferReason + "," + "\'"
				+ loggedinUser + "\'" + ", GETDATE()," + "\'" + loggedinUser + "\'" + ", GETDATE(),1," + witness1ID
				+ "," + witness2ID + ",null,null" + " from Batch b" + " left join BatchEvidence be "
				+ " ON b.BatchID = be.BatchID " + " left join Evidence e "
				+ " ON be.FSLabNum = e.FSLabNum and be.EvidenceType = e.EvidenceType and be.EvidenceID = e.EvidenceID  "
				+ " where b.BatchID = " + batchID);
		sql.append(" union ");
		sql.append(" select " + "convert(DATETIME," + "\'" + todaysDate + "\'" + ",20)" + " , " + newBatchId + " , "
				+ "convert(DATETIME," + "\'" + todaysDate + "\'" + ",20)" + ", " + "\'" + evidenceTransferTypeCode
				+ "\'" + " as TT " + ",'C'," + employeeID + "," + "null," + locationID + "," + locationID + ","
				+ organizationID + "," + organizationID + "," + "null," + storageAreaID + ","
				+ (StringUtils.isNotEmpty(storageLocationID) ? ("\'" + storageLocationID + "\'") : null) + ",null,"
				+ "e.FSLabNum, e.CurrentSubmissionNum,e.EvidenceType,e.EvidenceID,"
				+ (StringUtils.isNotEmpty(comments) ? ("\'" + comments + "\'") : null) + ",null," + transferReason + ","
				+ "\'" + loggedinUser + "\'" + ", GETDATE()," + "\'" + loggedinUser + "\'" + ", GETDATE(),1,"
				+ witness1ID + "," + witness2ID + ",null,null" + " from Batch b" + " left join BatchEvidence be "
				+ " ON b.BatchID = be.BatchID " + " left join Evidence e "
				+ " ON be.FSLabNum = e.FSLabNum and be.EvidenceType = e.EvidenceType and be.EvidenceID = e.EvidenceID  "
				+ " where b.BatchID = " + batchID);
		sql.append(" order by TT desc ");
		System.out.println("EvidenceTransfer Table Query :  " + sql.toString());
		return sql.toString();
	}

	public String setQueryForEvidenceTable(Integer batchID, Integer employeeID, Integer storageAreaID,
			String storageLocationID, Integer locationID, Integer organizationID, Integer newBatchID) {
		StringBuilder sql = new StringBuilder("Update Evidence" + " set " + " Evidence.EvidenceTransferID = "
				+ " (select max(EvidenceTransferID) from EvidenceTransfer et where et.BatchID = " + newBatchID + " and "
				+ " et.FSLabNum = Evidence.FSLabNum and et.EvidenceID = Evidence.EvidenceID and et.EvidenceType = Evidence.EvidenceType and et.FromEmployeeID = "
				+ employeeID + " and et.ToEmployeeID is null ), " + " Evidence.CustodyEmployeeID = " + "( CASE "
				+ "		WHEN Evidence.EvidenceStatusCode <> 'S' THEN " + employeeID + "		ELSE NULL " + "	END " + ") "
				+ "," + " Evidence.CustodyLocationID = " + locationID + "," + " Evidence.CustodyOrganizationID = "
				+ organizationID + "," + " Evidence.CustodyStorageAreaID = (CASE "
				+ " WHEN Evidence.EvidenceStatusCode IN ('S', 'V') THEN " + storageAreaID + " ELSE NULL " + " END), "
				+ " CustodyStorageLocationCode = "
				+ (StringUtils.isEmpty(storageLocationID) ? null
						: "(CASE " + " WHEN Evidence.EvidenceStatusCode IN ('S', 'V') THEN " + "\'" + storageLocationID
								+ "\'" + " ELSE NULL " + " END)")
				+ ", " + " LastModifiedBy = " + employeeID + "," + " LastModifiedDate = GETDATE() "
				+ " from BatchEvidence be  " + " join EvidenceTransfer et on  " + " et.FSLabNum=be.FSLabNum "
				+ " and et.EvidenceID=be.EvidenceID " + " and et.EvidenceType=be.EvidenceType and et.BatchID = "
				+ newBatchID + " where be.BatchID = " + batchID + " and Evidence.FSLabNum=be.FSLabNum "
				+ " and Evidence.EvidenceID=be.EvidenceID " + " and Evidence.EvidenceType=be.EvidenceType ");
		System.out.println("Evidence Table Update Query :  " + sql.toString());
		return sql.toString();
	}

	public boolean transferEvidence(String evidenceTransferQuery, String evidenceQuery)
			throws BaseApplicationException {
		Session session = openSession();
		session.beginTransaction();
		boolean flag = true;
		try {
			// track =
			// batchTransferTrackerRepository.createBatchTransferTracker(tracker);
			SQLQuery sqlQueryForEvidenceTransfer = session.createSQLQuery(evidenceTransferQuery);
			sqlQueryForEvidenceTransfer.executeUpdate();
			SQLQuery sqlQueryForEvidence = session.createSQLQuery(evidenceQuery);
			sqlQueryForEvidence.executeUpdate();
			session.getTransaction().commit();
		} catch (Exception e) {
			flag = false;
			session.getTransaction().rollback();
			logger.error("Transfer Unsuccessful !! An error occured while transfering the evidence.Exception : "
					+ e.getMessage());
		}
		// } finally {
		// if (track != null) {
		// track.setEndTime(new Timestamp(System.currentTimeMillis()));
		// track.setIsActive(false);
		// batchTransferTrackerRepository.updateBatchTransferTracker(track);
		// }
		// }
		if (session.isOpen()) {
			session.close();
		}
		return flag;
	}

}
