package com.nprotech.moneytracker.ui.activities;

import com.nprotech.moneytracker.ui.common.BaseActivity;

public class ManageBackupActivity_old extends BaseActivity {

//    private AppCompatImageView icBack;
//    private MaterialButton btnCreateBackup;
//    private RecyclerView rvBackup;
//    private RecyclerViewAdapter<BackupModel> backupAdapter;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_manage_backup);
//        statusBarDarkSetting();
//        hideKeyboard(this);
//
//        initComponents();
//    }
//
//    private void initComponents() {
//        try {
//            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
//            AppCompatTextView tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);
//            icBack = toolbarWrapper.findViewById(R.id.icBack);
//            AppCompatImageView ivAttach = toolbarWrapper.findViewById(R.id.ivAttach);
//
//            btnCreateBackup = findViewById(R.id.btnCreateBackup);
//            rvBackup = findViewById(R.id.rvBackup);
//
//            tvTitle.setText(getString(R.string.manage_backup));
//            ivAttach.setVisibility(View.VISIBLE);
//
//            ViewCompat.setOnApplyWindowInsetsListener(toolbarWrapper, (v, insets) -> {
//                int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
//                v.setPadding(v.getPaddingLeft(), top, v.getPaddingRight(), v.getPaddingBottom());
//                return insets;
//            });
//
//            initializeAdapters();
//            loadBackupData();
//            setUpListeners();
//        } catch (Exception e) {
//            AppLogger.e(getClass(), "initComponents", e);
//        }
//    }
//
//    private void initializeAdapters() {
//        try {
//            rvBackup.setLayoutManager(new LinearLayoutManager(this));
//
//            backupAdapter = new RecyclerViewAdapter<>(this, new ArrayList<>(), R.layout.item_account_currency) {
//                @Override
//                public void onPostBindViewHolder(ViewHolder holder, BackupModel backupModel) {
//                    holder.setViewText(R.id.tvCurrencyName, backupModel.fileName);
//                }
//            };
//            rvBackup.setAdapter(backupAdapter);
//            rvBackup.setHasFixedSize(true);
//            rvBackup.setItemAnimator(null);
//            rvBackup.addItemDecoration(new SimpleDividerItemDecoration(this));
//        } catch (Exception e) {
//            AppLogger.e(getClass(), "initializeAdapters", e);
//        }
//    }
//
//    private void setUpListeners() {
//        try {
//            icBack.setOnClickListener(view -> {
//                finish();
//                ActivityUtils.overrideCloseTransition(ManageBackupActivity_old.this, R.anim.scale_in, R.anim.right_to_left);
//            });
//
//            btnCreateBackup.setOnClickListener(view -> {
//                String fileName = getString(R.string.app_name).toLowerCase() + "_db_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".mtbackup";
//                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
//                intent.addCategory(Intent.CATEGORY_OPENABLE);
//                intent.setType("application/octet-stream");
//                intent.putExtra(Intent.EXTRA_TITLE, fileName);
//                createBackupLauncher.launch(intent);
//            });
//
//            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
//                @Override
//                public void handleOnBackPressed() {
//                    finish();
//                    ActivityUtils.overrideCloseTransition(ManageBackupActivity_old.this, R.anim.scale_in, R.anim.right_to_left);
//                }
//            });
//        } catch (Exception e) {
//            AppLogger.e(getClass(), "setUpListeners", e);
//        }
//    }
//
//    private void loadBackupData() {
//
//        try {
//
//            String uriString = getSharedPreferences("backup", MODE_PRIVATE).getString("backup_folder_uri", null);
//
//            if (uriString == null) {
//                backupAdapter.setItems(new ArrayList<>());
//                return;
//            }
//
//            Uri folderUri = Uri.parse(uriString);
//            DocumentFile folder = DocumentFile.fromTreeUri(this, folderUri);
//
//            if (folder == null || !folder.exists()) {
//                backupAdapter.setItems(new ArrayList<>());
//                return;
//            }
//
//            List<BackupModel> backupList = new ArrayList<>();
//
//            for (DocumentFile file : folder.listFiles()) {
//
//                if (!file.isFile())
//                    continue;
//
//                String name = file.getName();
//
//                if (name == null || !name.toLowerCase().endsWith(".mtbackup"))
//                    continue;
//
//                BackupModel model = new BackupModel();
//                model.fileName = name;
//                model.filePath = file.getUri().toString(); // Store Uri
//                model.fileSize = file.length();
//                model.lastModified = file.lastModified();
//
//                backupList.add(model);
//            }
//
//            Collections.sort(backupList, (o1, o2) -> Long.compare(o2.lastModified, o1.lastModified));
//            backupAdapter.setItems(backupList);
//
//        } catch (Exception e) {
//            AppLogger.e(getClass(), "loadBackupData", e);
//        }
//    }
//
//    private void exportBackup(Uri backupUri) {
//
//        Executors.newSingleThreadExecutor().execute(() -> {
//
//            try {
//
//                MoneyTrackerDatabase.getInstance(getApplicationContext()).close();
//
//                // Use your actual Room database name here
//                File db = getDatabasePath(getString(R.string.app_name) + "_db".toLowerCase());
//
//                if (!db.exists()) {
//                    runOnUiThread(() -> Toast.makeText(this, R.string.database_not_found, Toast.LENGTH_SHORT).show());
//                    return;
//                }
//
//                try (OutputStream outputStream = getContentResolver().openOutputStream(backupUri);
//                     ZipOutputStream zos = new ZipOutputStream(outputStream)) {
//
//                    addFileToZip(db, zos, "database/" + db.getName());
//
//                    File wal = new File(db.getAbsolutePath() + "-wal");
//                    if (wal.exists()) {
//                        addFileToZip(wal, zos, "database/" + wal.getName());
//                    }
//
//                    File shm = new File(db.getAbsolutePath() + "-shm");
//                    if (shm.exists()) {
//                        addFileToZip(shm, zos, "database/" + shm.getName());
//                    }
//                }
//                runOnUiThread(() -> Toast.makeText(this, R.string.database_exported_successfully, Toast.LENGTH_SHORT).show());
//            } catch (Exception e) {
//                AppLogger.e(getClass(), "exportBackup", e);
//                runOnUiThread(() -> Toast.makeText(this, R.string.backup_failed, Toast.LENGTH_SHORT).show());
//            }
//        });
//    }
//
//    private void addFileToZip(File file, ZipOutputStream zos, String zipEntryName) {
//        if (!file.exists()) return;
//
//        try (FileInputStream fis = new FileInputStream(file)) {
//
//            ZipEntry zipEntry = new ZipEntry(zipEntryName);
//            zos.putNextEntry(zipEntry);
//
//            byte[] buffer = new byte[8192];
//            int length;
//            while ((length = fis.read(buffer)) > 0) {
//                zos.write(buffer, 0, length);
//            }
//
//            zos.closeEntry();
//
//            AppLogger.d(getClass(), "Backup Exists = " + file.exists());
//            AppLogger.d(getClass(), "Backup Size = " + file.length());
//        } catch (Exception e) {
//            AppLogger.e(getClass(), "addFileToZip", e);
//        }
//    }
//
//    private final ActivityResultLauncher<Intent> createBackupLauncher = registerForActivityResult(
//            new ActivityResultContracts.StartActivityForResult(), result -> {
//
//                if (result.getResultCode() != RESULT_OK)
//                    return;
//
//                Intent data = result.getData();
//
//                if (data == null || data.getData() == null)
//                    return;
//
//                exportBackup(data.getData());
//
//            });
}