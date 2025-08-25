package com.example.keyboardtrainer.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.example.keyboardtrainer.R;
import com.example.keyboardtrainer.activity.LoginActivity;
import com.example.keyboardtrainer.activity.SettingsActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import static android.content.Context.MODE_PRIVATE;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AccountSettingsDialog extends BottomSheetDialogFragment {
    // Constants
    private static final String PREFS_NAME = "KeyboardTrainerPrefs";
    private static final String USER_NAME_KEY = "userName";
    private static final String IS_GUEST_KEY = "is_guest";

    // Lifecycle
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_account_settings, container, false);

        setupButtonListeners(view);
        setupUserInfo(view);

        return view;
    }

    // UI
    private void setupButtonListeners(View view) {
        view.findViewById(R.id.nameSettingsButton).setOnClickListener(v -> {
            SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            boolean isGuest = prefs.getBoolean(IS_GUEST_KEY, false);

            if (isGuest) {
                showGuestRestrictionDialog();
            } else {
                showNameDialog();
            }
        });

        view.findViewById(R.id.deleteAccountButton).setOnClickListener(v -> showDeleteConfirmation());
    }

    private void setupUserInfo(View view) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            setupUserTextViews(view, user);
            setupAccountTimestamp(view, user);
        }
    }

    /** @noinspection CodeBlock2Expr*/
    private void setupUserTextViews(View view, FirebaseUser user) {
        TextView userNameText = view.findViewById(R.id.userNameText);
        TextView userEmailText = view.findViewById(R.id.userEmailText);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    String username = document.getString("username");
                    String email = user.getEmail();

                    userNameText.setText(
                            username != null ? username :
                                    (user.getDisplayName() != null ? user.getDisplayName() :
                                            (email != null ? email.split("@")[0] : getString(R.string.default_user_name)))
                    );

                    userEmailText.setText(email != null ? email : "");
                })
                .addOnFailureListener(e -> {
                    handleFirestoreError(user, userNameText, userEmailText, e);
                });
    }

    private void handleFirestoreError(FirebaseUser user, TextView userNameText, TextView userEmailText, Exception e) {
        String name = user.getDisplayName();
        String email = user.getEmail();
        userNameText.setText(
                name != null ? name :
                        (email != null ? email.split("@")[0] : getString(R.string.default_user_name))
        );
        userEmailText.setText(email != null ? email : "");
        Log.e("AccountSettingsDialog", "Firestore error", e);
    }

    /** @noinspection DataFlowIssue*/
    private void setupAccountTimestamp(View view, FirebaseUser user) {
        TextView accountDateText = view.findViewById(R.id.accountDateText);
        TextView accountTimeText = view.findViewById(R.id.accountTimeText);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("ru"));
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        Date creationDate = new Date(user.getMetadata().getCreationTimestamp());

        accountDateText.setText(dateFormat.format(creationDate));
        accountTimeText.setText(timeFormat.format(creationDate));
    }

    // Dialog
    private void showGuestRestrictionDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());

        @SuppressLint("InflateParams") View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.guest_restriction_dialog, null);
        dialog.setContentView(dialogView);

        setupGuestDialog(dialog, dialogView);
        dialog.show();

        View parent = (View) dialogView.getParent();
        parent.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    private void setupGuestDialog(BottomSheetDialog dialog, View dialogView) {
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        MaterialButton loginButton = dialogView.findViewById(R.id.btn_login);
        MaterialButton continueButton = dialogView.findViewById(R.id.btn_continue);

        loginButton.setOnClickListener(v -> {
            navigateToLogin();
            dialog.dismiss();
            dismiss();
        });

        continueButton.setOnClickListener(v -> dialog.dismiss());
    }

    private void navigateToLogin() {
        startActivity(new Intent(getActivity(), LoginActivity.class));
    }

    private void showNameDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme);
        @SuppressLint("InflateParams") View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.bottom_sheet_edit_name, null);
        dialog.setContentView(dialogView);

        setupNameDialog(dialog, dialogView);
        dialog.show();

        View parent = (View) dialogView.getParent();
        parent.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    private void setupNameDialog(BottomSheetDialog dialog, View dialogView) {
        TextInputEditText nameInput = dialogView.findViewById(R.id.nameInput);
        MaterialButton saveButton = dialogView.findViewById(R.id.saveNameButton);

        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String currentName = prefs.getString(USER_NAME_KEY, "");
        nameInput.setText(currentName);

        setupNameInputFocus(dialog, nameInput);
        setupSaveButton(nameInput, saveButton, dialog, currentName);
    }

    private void setupNameInputFocus(BottomSheetDialog dialog, TextInputEditText nameInput) {
        dialog.setOnShowListener(dialogInterface -> {
            nameInput.requestFocus();
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(nameInput, InputMethodManager.SHOW_IMPLICIT);
        });
    }

    /** @noinspection DataFlowIssue*/
    private void setupSaveButton(TextInputEditText nameInput, MaterialButton saveButton,
                                 BottomSheetDialog dialog, String currentName) {
        saveButton.setOnClickListener(v -> {
            String newName = nameInput.getText().toString().trim();

            if (newName.isEmpty()) {
                nameInput.setError(getString(R.string.name_error_empty));
                return;
            }

            if (newName.equals(currentName)) {
                dialog.dismiss();
                return;
            }

            saveUserName(newName);
            dialog.dismiss();
        });
    }

    // User
    private void saveUserName(String name) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(getContext(), R.string.error_auth_not_authenticated, Toast.LENGTH_SHORT).show();
            return;
        }

        if (name.isEmpty()) {
            Toast.makeText(getContext(), R.string.error_empty_name, Toast.LENGTH_SHORT).show();
            return;
        }

        saveToSharedPreferences(name);
        updateFirebaseProfile(firebaseUser, name);
    }

    private void saveToSharedPreferences(String name) {
        SharedPreferences.Editor editor = requireActivity()
                .getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit();
        editor.putString(USER_NAME_KEY, name);
        editor.apply();
    }

    private void updateFirebaseProfile(FirebaseUser firebaseUser, String name) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();

        firebaseUser.updateProfile(profileUpdates)
                .addOnCompleteListener(authTask -> {
                    if (!authTask.isSuccessful()) {
                        handleAuthUpdateError(authTask);
                        return;
                    }

                    updateFirestoreUsername(firebaseUser, name);
                });
    }

    private void handleAuthUpdateError(com.google.android.gms.tasks.Task<Void> authTask) {
        String error = authTask.getException() != null ?
                authTask.getException().getMessage() : "Unknown error";
        Toast.makeText(getContext(),
                getString(R.string.error_auth_update_failed, error),
                Toast.LENGTH_SHORT).show();
    }

    private void updateFirestoreUsername(FirebaseUser firebaseUser, String name) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(firebaseUser.getUid())
                .update("username", name)
                .addOnCompleteListener(firestoreTask -> {
                    if (firestoreTask.isSuccessful()) {
                        handleSuccessNameUpdate(name);
                    } else {
                        handleFirestoreUpdateError(firestoreTask);
                    }
                });
    }

    private void handleSuccessNameUpdate(String name) {
        Toast.makeText(getContext(),
                R.string.success_name_updated,
                Toast.LENGTH_SHORT).show();

        if (getActivity() instanceof SettingsActivity) {
            ((SettingsActivity) getActivity()).updateUserName(name);
        }

        dismiss();
    }

    private void handleFirestoreUpdateError(com.google.android.gms.tasks.Task<Void> firestoreTask) {
        String error = firestoreTask.getException() != null ?
                firestoreTask.getException().getMessage() : "Unknown error";
        Toast.makeText(getContext(),
                getString(R.string.error_firestore_update_failed, error),
                Toast.LENGTH_SHORT).show();
    }

    // Delete
    private void showDeleteConfirmation() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme);
        @SuppressLint("InflateParams") View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_confirm_action, null);
        dialog.setContentView(dialogView);

        setupDeleteDialog(dialog, dialogView);
        dialog.show();

        View parent = (View) dialogView.getParent();
        parent.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    private void setupDeleteDialog(BottomSheetDialog dialog, View dialogView) {
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        MaterialButton btnConfirm = dialogView.findViewById(R.id.btn_confirm);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            triggerAccountDeletion();
            dialog.dismiss();
            dismiss();
        });
    }

    private void triggerAccountDeletion() {
        if (getActivity() instanceof SettingsActivity) {
            ((SettingsActivity) getActivity()).deleteAccount();
        }
    }
}