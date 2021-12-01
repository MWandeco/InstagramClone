package com.example.instagramclone.activity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.instagramclone.R;
import com.example.instagramclone.model.Postagem;
import com.example.instagramclone.model.Usuario;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class VisualizarPostagemActivity extends AppCompatActivity {
    private TextView textPerfilPostagem, textQuantidadeCurtidas,
            textDescricaoPostagem;
    private ImageView imagemPostagemSelecionada;
    private CircleImageView imagePerfilPostagem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizar_postagem);

        //Configura toolbar
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Perfil");
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_preto);


        //inicializar componentes
        inicializarComponentes();
        //recuperar dados da activity
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            Postagem postagem = (Postagem) bundle.getSerializable("postagem");
            Usuario usuario = (Usuario) bundle.getSerializable("usuario");

            //exibe dados do usuário logado
            Uri uri = Uri.parse(usuario.getCaminhoFoto());
            Glide.with(VisualizarPostagemActivity.this)
                    .load(uri)
                    .into(imagePerfilPostagem);
            textPerfilPostagem.setText(usuario.getNome());

            //exibe dados da postagem
            Uri uriPostagem = Uri.parse(postagem.getCaminhoDaFoto());
            Glide.with(VisualizarPostagemActivity.this)
                    .load(uriPostagem)
                    .into(imagemPostagemSelecionada);
            textDescricaoPostagem.setText(postagem.getDescricao());

        }
    }

    private void inicializarComponentes() {
        textPerfilPostagem = findViewById(R.id.textPerfilPostagem);
        textQuantidadeCurtidas = findViewById(R.id.textQuantidadeCurtidas);
        textDescricaoPostagem = findViewById(R.id.textDescricaoPostagem);
        imagemPostagemSelecionada = findViewById(R.id.imagemPostagemSelecionada);
        imagePerfilPostagem = findViewById(R.id.imagePerfilPostagem);
    }
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }
}