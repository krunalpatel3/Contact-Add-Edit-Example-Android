package example.krunal.addeditcontacts;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateActivity extends AppCompatActivity {

    private EditText editText_name, editText_Number, editText_Email,
            editText_Address, editText_Note, editText_Old_Phone_Number;
    private Button button_save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        editText_name = findViewById(R.id.editText_name);
        editText_Number = findViewById(R.id.editText_Number);
        editText_Email = findViewById(R.id.editText_Email);
        editText_Address = findViewById(R.id.editText_Address);
        editText_Note = findViewById(R.id.editText_Note);
        editText_Old_Phone_Number = findViewById(R.id.editText_Old_Phone_Number);
        button_save = findViewById(R.id.button_save);

        button_save.setOnClickListener((view) -> {

            if (validation()) {

                String getContectId = getContactIdByNunber();
                Log.d("getContectId", getContectId);

                boolean getResult = updateContact(editText_name.getText().toString().trim(),
                        editText_Number.getText().toString().trim(),
                        editText_Email.getText().toString().trim(),
                        editText_Address.getText().toString().trim(),
                        editText_Note.getText().toString().trim(),
                        getContectId);

                if (getResult) {
                    Toast.makeText(UpdateActivity.this, "Contact Updated Successfully", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(UpdateActivity.this, "Contact Not Updated!", Toast.LENGTH_LONG).show();
                }

            }

        });


    }

    public boolean updateContact(String name, String number, String email,
                                 String Address,
                                 String note,
                                 String ContactId) {
        boolean success = true;
        String phnumexp = "^[0-9]*$";

        try {
            name = name.trim();
            email = email.trim();
            number = number.trim();


            if (name.equals("") && number.equals("") && email.equals("")) {
                success = false;
            } else if ((!number.equals("")) && (!match(number, phnumexp))) {
                success = false;
            } else if ((!email.equals("")) && (!isEmailValid(email))) {
                success = false;
            }

//            else if (!Address.equals("")){
//                success = false;
//            } else if (!note.equals("")){
//                success = false;
//            }
            else {
                ContentResolver contentResolver = getContentResolver();

                String where = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";

                String[] emailParams = new String[]{ContactId, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE};
                String[] nameParams = new String[]{ContactId, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE};
                String[] numberParams = new String[]{ContactId, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE};
                String[] AddressParams = new String[]{ContactId, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE};
                String[] NoteParams = new String[]{ContactId, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE};

                ArrayList<ContentProviderOperation> ops = new ArrayList<android.content.ContentProviderOperation>();

                if (!email.equals("")) {
                    ops.add(android.content.ContentProviderOperation.newUpdate(android.provider.ContactsContract.Data.CONTENT_URI)
                            .withSelection(where, emailParams)
                            .withValue(ContactsContract.CommonDataKinds.Email.DATA, email)
                            .build());
                }

                if (!name.equals("")) {
                    ops.add(android.content.ContentProviderOperation.newUpdate(android.provider.ContactsContract.Data.CONTENT_URI)
                            .withSelection(where, nameParams)
                            .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                            .build());
                }

                if (!number.equals("")) {

                    ops.add(android.content.ContentProviderOperation.newUpdate(android.provider.ContactsContract.Data.CONTENT_URI)
                            .withSelection(where, numberParams)
                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
                            .build());
                }


                ops.add(android.content.ContentProviderOperation.newUpdate(android.provider.ContactsContract.Data.CONTENT_URI)
                        .withSelection(where, AddressParams)
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS, Address)
                        .build());


                ops.add(android.content.ContentProviderOperation.newUpdate(android.provider.ContactsContract.Data.CONTENT_URI)
                        .withSelection(where, NoteParams)
                        .withValue(ContactsContract.CommonDataKinds.Note.NOTE, note)
                        .build());


                contentResolver.applyBatch(ContactsContract.AUTHORITY, ops);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Exception", e.getMessage());
            success = false;
        }
        return success;
    }

    private String getContactIdByNunber() {

        ContentResolver contentResolver = getContentResolver();

        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(editText_Old_Phone_Number.getText().toString().trim()));

        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID};

        Cursor cursor =
                contentResolver.query(
                        uri,
                        projection,
                        null,
                        null,
                        null);
        String contactName = "";
        String contactId = "";

        if (cursor != null) {
            while (cursor.moveToNext()) {
                contactName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME));
                contactId = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
                Log.d("contactName", "contactMatch name: " + contactName);
                Log.d("contactId", "contactMatch id: " + contactId);
            }
            cursor.close();
        }

        return contactId;

    }

    private boolean validation() {
        Boolean result = true;
        //branch validation

        if (editText_Old_Phone_Number.getText() == null ||
                editText_Old_Phone_Number.getText().toString().isEmpty()) {
            editText_Old_Phone_Number.setError("Old Mobile No Required");
            editText_Old_Phone_Number.requestFocus();
            return false;
        }

        return result;
    }


    private boolean isEmailValid(String email) {
        String emailAddress = email.toString().trim();
        if (emailAddress == null)
            return false;
        else if (emailAddress.equals(""))
            return false;
        else if (emailAddress.length() <= 6)
            return false;
        else {
            String expression = "^[a-z][a-z|0-9|]*([_][a-z|0-9]+)*([.][a-z|0-9]+([_][a-z|0-9]+)*)?@[a-z][a-z|0-9|]*\\.([a-z][a-z|0-9]*(\\.[a-z][a-z|0-9]*)?)$";
            CharSequence inputStr = emailAddress;
            Pattern pattern = Pattern.compile(expression,
                    Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(inputStr);
            if (matcher.matches())
                return true;
            else
                return false;
        }
    }

    private boolean match(String stringToCompare, String regularExpression) {
        boolean success = false;
        Pattern pattern = Pattern.compile(regularExpression);
        Matcher matcher = pattern.matcher(stringToCompare);
        if (matcher.matches())
            success = true;
        return success;
    }
}
