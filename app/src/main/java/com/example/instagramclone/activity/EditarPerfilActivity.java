package com.example.instagramclone.activity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.instagramclone.R;
import com.example.instagramclone.helper.ConfiguracaoFirebase;
import com.example.instagramclone.helper.Permissao;
import com.example.instagramclone.helper.UsuarioFirebase;
import com.example.instagramclone.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditarPerfilActivity extends AppCompatActivity {

    private CircleImageView imageEditarPerfil;
    private TextView textAlterarFoto,textEmailPerfil;
    private TextInputEditText editNomePerfil;
    private Button buttonSalvarAlteracoes;
    private Usuario usuarioLogado;
    public static final int SELECAO_CAMERA = 200;
    private StorageReference storageRef;
    private String identificadorUsuario;
    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);

        inicializarComponentes();
        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();
        storageRef = ConfiguracaoFirebase.getFirebaseStorageReference();
        identificadorUsuario = UsuarioFirebase.getIdentificadorUsuario();

        //configura Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Editar Perfil");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_preto);

        //validar permissoes
        Permissao.validarPermissoes(permissoesNecessarias,this,1);

        //recuperar dados do usuario
        FirebaseUser usuarioPerfil = UsuarioFirebase.getUsuarioAtual();
        editNomePerfil.setHint(usuarioPerfil.getDisplayName());
        textEmailPerfil.setText(usuarioPerfil.getEmail());
        Uri url = usuarioPerfil.getPhotoUrl();
        if(url != null){
            Glide.with(EditarPerfilActivity.this)
                    .load(url)
                    .into(imageEditarPerfil);
        }else {
            imageEditarPerfil.setImageResource(R.drawable.avatar);
        }

        //salvar alterações
        buttonSalvarAlteracoes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nomeAtualizado = editNomePerfil.getText().toString();
                UsuarioFirebase.atualizarNomeUsuario(nomeAtualizado);
                //atualizar nome no banco de dados
                usuarioLogado.setNome(nomeAtualizado);
                usuarioLogado.atualizar();
                Toast.makeText(getApplicationContext(), "Dados Atualizados!", Toast.LENGTH_SHORT).show();
            }
        });

        //alterar foto
        textAlterarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if(i.resolveActivity(getPackageManager())!= null){
                    startActivityForResult(i,SELECAO_CAMERA);
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            Bitmap imagem = null;

            try{
                //selecao da galeria apenas
                switch (requestCode){
                    case SELECAO_CAMERA:
                       Uri localImagemSelecionada =data.getData();
                       imagem = MediaStore.Images.Media.getBitmap(getContentResolver(),localImagemSelecionada);

                        break;
                }
                if(imagem != null){
                    imageEditarPerfil.setImageBitmap(imagem);
                    //recuperar dados da imagem para o firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG,100,baos);
                    byte[] dadosImagem = baos.toByteArray();
                    //salvar imagem no firebase
                    final StorageReference imagemRef = storageRef
                            .child("imagens")
                            .child("perfil")
                            .child(identificadorUsuario +".jpeg");
                    UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(),
                                    "Falha ao baixar imagem", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //recuperar local da foto
                            imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    Uri url = task.getResult();
                                    atualizarFotoUsuario(url);

                                }
                            });
                            Toast.makeText(getApplicationContext(),
                                    "Sucesso ao alterar foto", Toast.LENGTH_SHORT).show();
                        }
                    });

                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void inicializarComponentes(){
        imageEditarPerfil = findViewById(R.id.imageEditarPerfil);
        textAlterarFoto = findViewById(R.id.textAlterarFoto);
        textEmailPerfil = findViewById(R.id.textEmailPerfil);
        editNomePerfil = findViewById(R.id.editNomePerfil);
        buttonSalvarAlteracoes = findViewById(R.id.buttonSalvarAlterações);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }
    private void atualizarFotoUsuario(Uri url){
        //atualiza foto no perfil
        UsuarioFirebase.atualizarFotoUsuario(url);
        //atualiza foto no firebase
        usuarioLogado.setCaminhoFoto(url.toString());
        usuarioLogado.atualizar();

        Toast.makeText(getApplicationContext(),
                "Foto Atualizada com Sucesso",
                Toast.LENGTH_SHORT).show();

    }
}