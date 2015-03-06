package com.example.josu.abenhumeyav3;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.query.Query;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;


public class VerPedido extends Activity {

    private ArrayList <Pedido> pedido;
    private ListView lv;
    private AdaptadorVerPedido adaptadorPedido;
    private int posicion;
    private TextView tv1, tv2;
    private ArrayList <Mesa> listaMesas;
    private ObjectContainer bd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_pedido);
        initComponents();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.ver_pedido, menu);
        bd= Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), getExternalFilesDir(null) + "/abenhumeya.db4o");
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater= getMenuInflater();
        inflater.inflate(R.menu.longclick_ver_pedido, menu);
    }

    //Esta es la opción eliminar del menú contextual para eliminar platos del pedido.
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id=item.getItemId();
        AdapterView.AdapterContextMenuInfo info= (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int index= info.position;
        Object o= info.targetView.getTag();
        AdaptadorVerPedido.ViewHolder vh;
        vh = (AdaptadorVerPedido.ViewHolder)o;
        tostada("En el menú contextual sí entra");
        if (id == R.id.action_eliminar) {
            tostada(getResources().getString(R.string.ttEliminado)+ " "+vh.tv1.getText().toString());
            pedido.remove(index);
            adaptadorPedido.notifyDataSetChanged();
            return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_guardar) {
            confirmarPedido();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void tostada(String cad){
        Toast.makeText(this, cad, Toast.LENGTH_SHORT).show();
    }

    /*Este es el método asociado al onClick del botón Confirmar pedido. Asignamos el pedido a la mesa que le corresponda y finalizamos la activity.*/
    public void confirmarPedido(){
        listaMesas.get(posicion).setPedidos(pedido);
        escribirBD();
        bd.close();
        finish();
    }

    public void initComponents(){
        /*Obtenemos de la activity de la que esta ha sido llamada la lista de mesas, su posición y el ArrayList pedido pero tenemos que asegurarnos de no machacar
        * el contenido previo si ya teníamos un pedido asignado a esta mesa. En tal caso, añadimos el nuevo pedido a continuación.*/
        listaMesas = (ArrayList <Mesa>)getIntent().getExtras().get("mesas");
        posicion = (Integer)getIntent().getExtras().get("posicion");
        if(listaMesas.get(posicion).getPedidos() == null)
            pedido = (ArrayList<Pedido>)getIntent().getExtras().get("pedido");

        else{
            pedido = listaMesas.get(posicion).getPedidos();
            ArrayList<Pedido> aux = (ArrayList<Pedido>)getIntent().getExtras().get("pedido");
            for (int i=0; i<aux.size(); i++)
                pedido.add(aux.get(i));
        }

        tv1 = (TextView)findViewById(R.id.tvMesa);
        tv2 = (TextView)findViewById(R.id.tvComensal);
        ArrayList <String> aux = new ArrayList (Arrays.asList(getResources().getStringArray(R.array.mesas)));
        tv1.setText(aux.get(listaMesas.get(posicion).getNumMesa()));
        aux = new ArrayList (Arrays.asList(getResources().getStringArray(R.array.comensales)));
        tv2.setText(aux.get(listaMesas.get(posicion).getNumComensal()));
        lv = (ListView)findViewById(R.id.lvVerPedido);
        adaptadorPedido = new AdaptadorVerPedido(this, R.layout.detalle_pedido, pedido);
        lv.setAdapter(adaptadorPedido);
        registerForContextMenu(lv);
    }

    public void escribirBD(){
        Query consulta = bd.query();
        consulta.constrain(Mesa.class);
        consulta.descend("numMesa").constrain(listaMesas.get(posicion).getNumMesa());
        ObjectSet<Mesa> mesas = consulta.execute();
        while(mesas.hasNext())
            bd.delete(mesas.next());
        bd.store(listaMesas.get(posicion));
        bd.commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        bd.close();
    }
}
