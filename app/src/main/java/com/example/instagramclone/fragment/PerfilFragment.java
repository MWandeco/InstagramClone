package com.example.instagramclone.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.instagramclone.R;
import com.example.instagramclone.activity.EditarPerfilActivity;
import com.example.instagramclone.adapter.AdapterGrid;
import com.example.instagramclone.helper.ConfiguracaoFirebase;
import com.example.instagramclone.helper.UsuarioFirebase;
import com.example.instagramclone.model.Postagem;
import com.example.instagramclone.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class PerfilFragment extends Fragment {
    private ProgressBar progressBar;
    private CircleImageView imagePerfil;
    public GridView gridViewPerfil;
    private TextView textPublicacoes, textSeguidores, textSeguindo;
    private Button buttonAcaoPerfil;

    private Usuario usuarioLogado;

    private DatabaseReference usuariosRef;
    private DatabaseReference usuarioLogadoRef;
    private DatabaseReference firebaseRef;
    private DatabaseReference postagensUsuarioRef;
    private ValueEventListener valueEventListenerPerfil;

    private AdapterGrid adapterGrid;


    public PerfilFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);


        //configurar componentes
        inicializarComponentes(view);

        //recupera usuario logado
        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();
        firebaseRef = ConfiguracaoFirebase.getFirebase();
        usuariosRef = firebaseRef.child("usuarios");

        //configurar referencia postagens usuario
        postagensUsuarioRef = ConfiguracaoFirebase
                .getFirebase()
                .child("postagens")
                .child(usuarioLogado.getId());



        //abre edição de perfil
        buttonAcaoPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), EditarPerfilActivity.class);
                startActivity(i);
            }
        });

        inicializarImageLoader();

        carregarFotosPostagem();

        return view;
    }

    public void inicializarImageLoader() {
        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration
                .Builder(getActivity())
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .memoryCacheSize(2 * 1024 * 1024)
                .diskCacheSize(50 * 1024 * 1024)
                .diskCacheFileCount(100)
                .diskCacheFileNameGenerator(new HashCodeFileNameGenerator())
                .build();
        ImageLoader.getInstance().init(configuration);
    }

    public void carregarFotosPostagem() {
        postagensUsuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //configurar tamanho do  grid
                int tamanhoGrid = getResources().getDisplayMetrics().widthPixels;
                int tamanhoImagem = tamanhoGrid / 3;
                gridViewPerfil.setColumnWidth(tamanhoImagem);

                List<String> urlFotos = new ArrayList<>();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Postagem postagem = ds.getValue(Postagem.class);
                    urlFotos.add(postagem.getCaminhoDaFoto());
                    //Log.i("postagem",postagem.getCaminhoDaFoto());
                }

                //configurar adapter
                adapterGrid = new AdapterGrid(getActivity(), R.layout.grid_postagem, urlFotos);
                gridViewPerfil.setAdapter(adapterGrid);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void inicializarComponentes(View view) {
        progressBar = view.findViewById(R.id.progressLogin);
        gridViewPerfil = view.findViewById(R.id.gridViewPerfil);
        imagePerfil = view.findViewById(R.id.imagePerfil);
        textPublicacoes = view.findViewById(R.id.textPublicacoes);
        textSeguidores = view.findViewById(R.id.textSeguidores);
        textSeguindo = view.findViewById(R.id.textSeguindo);
        buttonAcaoPerfil = view.findViewById(R.id.buttonAcaoPerfil);
    }
    private void recuperarFotoUsuario(){
        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();
        //Recuperar foto do usuário
        String caminhoFoto = usuarioLogado.getCaminhoFoto();
        if (caminhoFoto != null) {
            Uri url = Uri.parse(caminhoFoto);
            Glide.with(getActivity())
                    .load(url)
                    .into(imagePerfil);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarDadosUsuarioLogado();
        recuperarFotoUsuario();

    }

    @Override
    public void onStop() {
        super.onStop();
        usuarioLogadoRef.removeEventListener(valueEventListenerPerfil);
    }

    private void recuperarDadosUsuarioLogado() {

        usuarioLogadoRef = usuariosRef.child(usuarioLogado.getId());
        valueEventListenerPerfil = usuarioLogadoRef.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        Usuario usuario = dataSnapshot.getValue(Usuario.class);

                        String postagens = String.valueOf(usuario.getPostagens());
                        String seguindo = String.valueOf(usuario.getSeguindo());
                        String seguidores = String.valueOf(usuario.getSeguidores());

                        //Configura valores recuperados
                        textPublicacoes.setText(postagens);
                        textSeguidores.setText(seguidores);
                        textSeguindo.setText(seguindo);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );

    }
}