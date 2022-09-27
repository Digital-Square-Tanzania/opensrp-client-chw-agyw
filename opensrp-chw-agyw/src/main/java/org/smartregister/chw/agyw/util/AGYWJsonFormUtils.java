package org.smartregister.chw.agyw.util;

import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.agyw.AGYWLibrary;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.domain.tag.FormTag;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.util.FormUtils;

import static org.smartregister.chw.agyw.util.Constants.ENCOUNTER_TYPE;
import static org.smartregister.chw.agyw.util.Constants.STEP_EIGHT;
import static org.smartregister.chw.agyw.util.Constants.STEP_FIVE;
import static org.smartregister.chw.agyw.util.Constants.STEP_FOUR;
import static org.smartregister.chw.agyw.util.Constants.STEP_NINE;
import static org.smartregister.chw.agyw.util.Constants.STEP_ONE;
import static org.smartregister.chw.agyw.util.Constants.STEP_SEVEN;
import static org.smartregister.chw.agyw.util.Constants.STEP_SIX;
import static org.smartregister.chw.agyw.util.Constants.STEP_TEN;
import static org.smartregister.chw.agyw.util.Constants.STEP_THREE;
import static org.smartregister.chw.agyw.util.Constants.STEP_TWO;

public class AGYWJsonFormUtils extends org.smartregister.util.JsonFormUtils {
    public static final String METADATA = "metadata";

    public static Triple<Boolean, JSONObject, JSONArray> validateParameters(String jsonString) {

        JSONObject jsonForm = toJSONObject(jsonString);
        JSONArray fields = agywFormFields(jsonForm);

        Triple<Boolean, JSONObject, JSONArray> registrationFormParams = Triple.of(jsonForm != null && fields != null, jsonForm, fields);
        return registrationFormParams;
    }

    public static JSONArray agywFormFields(JSONObject jsonForm) {
        //TODO: refactor this implementation with a O(logN) complexity
        try {
            JSONArray fields = new JSONArray();
            JSONArray fieldsOne = fields(jsonForm, STEP_ONE);
            JSONArray fieldsTwo = fields(jsonForm, STEP_TWO);
            JSONArray fieldsThree = fields(jsonForm, STEP_THREE);
            JSONArray fieldsFour = fields(jsonForm, STEP_FOUR);
            JSONArray fieldsFive = fields(jsonForm, STEP_FIVE);
            JSONArray fieldsSix = fields(jsonForm, STEP_SIX);
            JSONArray fieldsSeven = fields(jsonForm, STEP_SEVEN);
            JSONArray fieldsEight = fields(jsonForm, STEP_EIGHT);
            JSONArray fieldsNine = fields(jsonForm, STEP_NINE);
            JSONArray fieldsTen = fields(jsonForm, STEP_TEN);

            compileFields(fields, fieldsOne);
            compileFields(fields, fieldsTwo);
            compileFields(fields, fieldsThree);
            compileFields(fields, fieldsFour);
            compileFields(fields, fieldsFive);
            compileFields(fields, fieldsSix);
            compileFields(fields, fieldsSeven);
            compileFields(fields, fieldsEight);
            compileFields(fields, fieldsNine);
            compileFields(fields, fieldsTen);

            return fields;

        } catch (JSONException e) {
            Log.e(TAG, "", e);
        }
        return null;
    }

    private static void compileFields(JSONArray compiledFields, JSONArray addedField) throws JSONException {
        if (addedField != null) {
            for (int i = 0; i < addedField.length(); i++) {
                compiledFields.put(addedField.get(i));
            }
        }
    }

    public static JSONArray fields(JSONObject jsonForm, String step) {
        try {

            JSONObject step1 = jsonForm.has(step) ? jsonForm.getJSONObject(step) : null;
            if (step1 == null) {
                return null;
            }

            return step1.has(FIELDS) ? step1.getJSONArray(FIELDS) : null;

        } catch (JSONException e) {
            Log.e(TAG, "", e);
        }
        return null;
    }

    public static Event processJsonForm(AllSharedPreferences allSharedPreferences, String
            jsonString) {

        Triple<Boolean, JSONObject, JSONArray> registrationFormParams = validateParameters(jsonString);

        if (!registrationFormParams.getLeft()) {
            return null;
        }

        JSONObject jsonForm = registrationFormParams.getMiddle();
        JSONArray fields = registrationFormParams.getRight();
        String entityId = getString(jsonForm, ENTITY_ID);
        String encounter_type = jsonForm.optString(Constants.JSON_FORM_EXTRA.ENCOUNTER_TYPE);

        if (Constants.EVENT_TYPE.AGYW_REGISTRATION.equals(encounter_type)) {
            encounter_type = Constants.TABLES.AGYW_REGISTER;
        } else if (Constants.EVENT_TYPE.AGYW_FOLLOW_UP_VISIT.equals(encounter_type)) {
            encounter_type = Constants.TABLES.AGYW_FOLLOW_UP;
        }
        return org.smartregister.util.JsonFormUtils.createEvent(fields, getJSONObject(jsonForm, METADATA), formTag(allSharedPreferences), entityId, getString(jsonForm, ENCOUNTER_TYPE), encounter_type);
    }

    protected static FormTag formTag(AllSharedPreferences allSharedPreferences) {
        FormTag formTag = new FormTag();
        formTag.providerId = allSharedPreferences.fetchRegisteredANM();
        formTag.appVersion = AGYWLibrary.getInstance().getApplicationVersion();
        formTag.databaseVersion = AGYWLibrary.getInstance().getDatabaseVersion();
        return formTag;
    }

    public static void tagEvent(AllSharedPreferences allSharedPreferences, Event event) {
        String providerId = allSharedPreferences.fetchRegisteredANM();
        event.setProviderId(providerId);
        event.setLocationId(locationId(allSharedPreferences));
        event.setChildLocationId(allSharedPreferences.fetchCurrentLocality());
        event.setTeam(allSharedPreferences.fetchDefaultTeam(providerId));
        event.setTeamId(allSharedPreferences.fetchDefaultTeamId(providerId));

        event.setClientApplicationVersion(AGYWLibrary.getInstance().getApplicationVersion());
        event.setClientDatabaseVersion(AGYWLibrary.getInstance().getDatabaseVersion());
    }

    private static String locationId(AllSharedPreferences allSharedPreferences) {
        String providerId = allSharedPreferences.fetchRegisteredANM();
        String userLocationId = allSharedPreferences.fetchUserLocalityId(providerId);
        if (StringUtils.isBlank(userLocationId)) {
            userLocationId = allSharedPreferences.fetchDefaultLocalityId(providerId);
        }

        return userLocationId;
    }

    public static void getRegistrationForm(JSONObject jsonObject, String entityId, String
            currentLocationId) throws JSONException {
        jsonObject.getJSONObject(METADATA).put(ENCOUNTER_LOCATION, currentLocationId);
        jsonObject.put(org.smartregister.util.JsonFormUtils.ENTITY_ID, entityId);
        jsonObject.put(DBConstants.KEY.RELATIONAL_ID, entityId);
    }

    public static JSONObject getFormAsJson(String formName) throws Exception {
        return FormUtils.getInstance(AGYWLibrary.getInstance().context().applicationContext()).getFormJson(formName);
    }

}
