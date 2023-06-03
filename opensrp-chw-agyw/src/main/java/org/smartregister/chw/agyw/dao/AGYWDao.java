package org.smartregister.chw.agyw.dao;

import org.smartregister.agyw.R;
import org.smartregister.chw.agyw.domain.MemberObject;
import org.smartregister.chw.agyw.util.Constants;
import org.smartregister.dao.AbstractDao;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AGYWDao extends AbstractDao {

    public static boolean isRegisteredForAgyw(String baseEntityID) {
        String sql = "SELECT count(p.base_entity_id) count FROM ec_agyw_register p " +
                "WHERE p.base_entity_id = '" + baseEntityID + "' AND p.is_closed = 0 ";

        DataMap<Integer> dataMap = cursor -> getCursorIntValue(cursor, "count");

        List<Integer> res = readData(sql, dataMap);
        if (res == null || res.size() != 1)
            return false;

        return res.get(0) > 0;
    }

    public static String getUIC_ID(String baseEntityId) {
        String sql = "SELECT uic_id FROM ec_agyw_register p " +
                " WHERE p.base_entity_id = '" + baseEntityId + "' AND p.is_closed = 0 ";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "uic_id");

        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() != 0 && res.get(0) != null) {
            return res.get(0);
        }
        return "";
    }


    public static MemberObject getMember(String baseEntityID) {
        String sql = "SELECT m.base_entity_id,\n" +
                "       m.unique_id,\n" +
                "       m.relational_id,\n" +
                "       m.dob,\n" +
                "       m.first_name,\n" +
                "       m.middle_name,\n" +
                "       m.last_name,\n" +
                "       m.gender,\n" +
                "       m.phone_number,\n" +
                "       m.other_phone_number,\n" +
                "       f.first_name     family_name,\n" +
                "       f.primary_caregiver,\n" +
                "       f.family_head,\n" +
                "       f.village_town,\n" +
                "       fh.first_name    family_head_first_name,\n" +
                "       fh.middle_name   family_head_middle_name,\n" +
                "       fh.last_name     family_head_last_name,\n" +
                "       fh.phone_number  family_head_phone_number,\n" +
                "       ancr.is_closed   anc_is_closed,\n" +
                "       pncr.is_closed   pnc_is_closed,\n" +
                "       pcg.first_name   pcg_first_name,\n" +
                "       pcg.last_name    pcg_last_name,\n" +
                "       pcg.middle_name  pcg_middle_name,\n" +
                "       pcg.phone_number pcg_phone_number,\n" +
                "       mr.*\n" +
                "FROM ec_family_member m\n" +
                "         INNER JOIN ec_family f on m.relational_id = f.base_entity_id\n" +
                "         INNER JOIN ec_agyw_register mr on mr.base_entity_id = m.base_entity_id\n" +
                "         LEFT JOIN ec_family_member fh on fh.base_entity_id = f.family_head\n" +
                "         LEFT JOIN ec_family_member pcg on pcg.base_entity_id = f.primary_caregiver\n" +
                "         LEFT JOIN ec_anc_register ancr on ancr.base_entity_id = m.base_entity_id\n" +
                "         LEFT JOIN ec_pregnancy_outcome pncr on pncr.base_entity_id = m.base_entity_id\n" +
                "WHERE m.base_entity_id = '" + baseEntityID + "' ";
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        DataMap<MemberObject> dataMap = cursor -> {
            MemberObject memberObject = new MemberObject();

            memberObject.setFirstName(getCursorValue(cursor, "first_name", ""));
            memberObject.setMiddleName(getCursorValue(cursor, "middle_name", ""));
            memberObject.setLastName(getCursorValue(cursor, "last_name", ""));
            memberObject.setAddress(getCursorValue(cursor, "village_town"));
            memberObject.setGender(getCursorValue(cursor, "gender"));
            memberObject.setUniqueId(getCursorValue(cursor, "unique_id", ""));
            memberObject.setDob(getCursorValue(cursor, "dob"));
            memberObject.setFamilyBaseEntityId(getCursorValue(cursor, "relational_id", ""));
            memberObject.setRelationalId(getCursorValue(cursor, "relational_id", ""));
            memberObject.setPrimaryCareGiver(getCursorValue(cursor, "primary_caregiver"));
            memberObject.setFamilyName(getCursorValue(cursor, "family_name", ""));
            memberObject.setPhoneNumber(getCursorValue(cursor, "phone_number", ""));
            memberObject.setAgywTestDate(getCursorValueAsDate(cursor, "agyw_test_date", df));
            memberObject.setBaseEntityId(getCursorValue(cursor, "base_entity_id", ""));
            memberObject.setFamilyHead(getCursorValue(cursor, "family_head", ""));
            memberObject.setFamilyHeadPhoneNumber(getCursorValue(cursor, "pcg_phone_number", ""));
            memberObject.setFamilyHeadPhoneNumber(getCursorValue(cursor, "family_head_phone_number", ""));
            memberObject.setAncMember(getCursorValue(cursor, "anc_is_closed", ""));
            memberObject.setPncMember(getCursorValue(cursor, "pnc_is_closed", ""));

            String familyHeadName = getCursorValue(cursor, "family_head_first_name", "") + " "
                    + getCursorValue(cursor, "family_head_middle_name", "");

            familyHeadName =
                    (familyHeadName.trim() + " " + getCursorValue(cursor, "family_head_last_name", "")).trim();
            memberObject.setFamilyHeadName(familyHeadName);

            String familyPcgName = getCursorValue(cursor, "pcg_first_name", "") + " "
                    + getCursorValue(cursor, "pcg_middle_name", "");

            familyPcgName =
                    (familyPcgName.trim() + " " + getCursorValue(cursor, "pcg_last_name", "")).trim();
            memberObject.setPrimaryCareGiverName(familyPcgName);

            return memberObject;
        };

        List<MemberObject> res = readData(sql, dataMap);
        if (res == null || res.size() != 1)
            return null;

        return res.get(0);
    }

    public static int getPackageStatus(String baseEntityId, int age) {
        String sql = "SELECT sbcc_intervention_provided, economic_empowerment_education FROM ec_agyw_register p " +
                "WHERE p.base_entity_id = '" + baseEntityId + "' AND p.is_closed = 0";

        DataMap<String> behavioralDataMap = cursor -> getCursorValue(cursor, "sbcc_intervention_provided");
        DataMap<String> structuralDataMap = cursor -> getCursorValue(cursor, "economic_empowerment_education");

        List<String> behavioralRes = readData(sql, behavioralDataMap);
        List<String> structuralRes = readData(sql, structuralDataMap);
        if ((behavioralRes != null && behavioralRes.size() > 0 && behavioralRes.get(0) != null) && (structuralRes != null && structuralRes.size() > 0 && structuralRes.get(0) != null)) {
            if (behavioralRes.get(0).contains(",") && structuralRes.get(0).contains(",")) {
                if (age < 15 && (
                        behavioralRes.get(0).trim().split(",").length >= Constants.DREAMS_PACKAGE.behavioral_services_10_14_keys.size() &&
                                structuralRes.get(0).trim().split(",").length >= Constants.DREAMS_PACKAGE.structural_services_10_14_keys.size())
                ) {
                    return R.string.primary_package_label;
                } else if (age >= 15 && (
                        behavioralRes.get(0).trim().split(",").length >= Constants.DREAMS_PACKAGE.behavioral_services_15_24_keys.size() &&
                                structuralRes.get(0).trim().split(",").length >= Constants.DREAMS_PACKAGE.structural_services_15_24_keys.size())
                ) {
                    return R.string.primary_package_label;
                }
            }
            return 0;
        }
        return 0;
    }

    public static boolean isEligibleToGraduateServices(String baseEntityId, int age) {
        //is eligible to graduate if package status != 0
        return getPackageStatus(baseEntityId, age) != 0;
    }

    public static String getEnrolledProgram(String baseEntityId) {
        String sql = "SELECT program_name FROM ec_agyw_register p " +
                " WHERE p.base_entity_id = '" + baseEntityId + "' AND p.is_closed = 0 ";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "program_name");

        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() != 0 && res.get(0) != null) {
            return res.get(0);
        }
        return "";
    }
}
