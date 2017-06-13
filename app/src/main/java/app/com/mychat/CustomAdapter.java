package app.com.mychat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Locale;

public class CustomAdapter extends BaseAdapter
{
    Context context;
    ArrayList<User> us;
    private ArrayList<User> filteredData = null;
    private static LayoutInflater inflater = null;
    String sUserId ;
    ProgressDialog progressDialog;
    SharedPreferences sharedpreferences;
    public ImageLoader imageLoader;

    public CustomAdapter(UserList mainActivity, ArrayList<User> us)
    {
         context = mainActivity;
         sharedpreferences = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
         sUserId = sharedpreferences.getString("mymobile","");
         this.us = us;
         this.filteredData = new ArrayList<User>();
         this.filteredData.addAll(us);
         inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         imageLoader=new ImageLoader(context.getApplicationContext());

    }
    @Override
    public int getCount()
    {
        return us.size();
    }

    @Override
    public Object getItem(int position)
    {
        return position;
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }
    public class Holder
    {
        TextView tv,eh,uid;
        ImageView img;
        Button info;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        final Holder holder=new Holder();
        View rowView;
        rowView = inflater.inflate(R.layout.user_design, null);
        holder.tv=(TextView) rowView.findViewById(R.id.text2);
        holder.eh=(TextView)rowView.findViewById(R.id.texthideemail);
        holder.uid=(TextView)rowView.findViewById(R.id.texthideobjectid);
        holder.img=(ImageView) rowView.findViewById(R.id.imageUser);
      //  holder.info=(Button) rowView.findViewById(R.id.buttonInfo);

         User u=us.get(position);
         holder.tv.setText(u.getUname());
         holder.eh.setText(u.getEmail());
         holder.uid.setText(u.getUid());

        // System.out.println("the image :" + u.getUimage());
       // imageLoader.DisplayImage(u.getUimage(), holder.img);

         Picasso.with(context).load(u.getUimage()).fit().transform(new CircleTransform()).placeholder(R.drawable.img_pic).into(holder.img);
         holder.img.setOnClickListener(new View.OnClickListener()
         {
            @Override
            public void onClick(View v)
            {
                try
                {
                    Intent i1 = new Intent(context, ChatRoomActivity.class);

                    Bundle b = new Bundle();
                    b.putString("name", (us.get(position).getUname().toString()));
                    b.putString("RECIPIENT_ID", (us.get(position).getUid().toString()));
                    b.putString("img",us.get(position).getUimage().toString());
                    b.putString("where","in");
                    i1.putExtras(b);

                  /*  i1.putExtra("name", );
                    i1.putExtra("RECIPIENT_ID", (us.get(position).getUid().toString()));
                    i1.putExtra("img",);
                    i1.putExtra("where","in");
                  */

                    sharedpreferences.edit().putString("userImg",us.get(position).getUimage().toString()).commit();
                    context.startActivity(i1);
                }
                catch (Exception e)
                {
                   System.out.print("Error 1 :" + e);
                }
            }
        });
        holder.tv.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    Intent i1 = new Intent(context, ChatRoomActivity.class);

                    Bundle b = new Bundle();
                    b.putString("name", (us.get(position).getUname().toString()));
                    b.putString("RECIPIENT_ID", (us.get(position).getUid().toString()));
                    b.putString("img",us.get(position).getUimage().toString());
                    b.putString("where","in");
                    i1.putExtras(b);

                   /* i1.putExtra("name", (us.get(position).getUname().toString()));
                    i1.putExtra("RECIPIENT_ID", (us.get(position).getUid().toString()));
                    i1.putExtra("img",us.get(position).getUimage().toString());
                    i1.putExtra("where","in");
*/
                    sharedpreferences.edit().putString("userImg",us.get(position).getUimage().toString()).commit();
                    context.startActivity(i1);
                }
                catch (Exception e)
                {
                    System.out.print("Error 1 :" + e);
                }
            }
        });

        rowView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent i1 = new Intent(context, ChatRoomActivity.class);
                Bundle b = new Bundle();
                b.putString("name", (us.get(position).getUname().toString()));
                b.putString("RECIPIENT_ID", (us.get(position).getUid().toString()));
                b.putString("img",us.get(position).getUimage().toString());
                b.putString("where","in");
                i1.putExtras(b);

               /* i1.putExtra("name", (us.get(position).getUname().toString()));
                i1.putExtra("RECIPIENT_ID", (us.get(position).getUid().toString()));
                i1.putExtra("img",us.get(position).getUimage().toString());
                i1.putExtra("where","in");
                */sharedpreferences.edit().putString("userImg",us.get(position).getUimage().toString()).commit();
                context.startActivity(i1);
            }
        });
        return rowView;
    }

    public void filter(String charText)
    {
        charText = charText.toLowerCase(Locale.getDefault());
        us.clear();

        if (charText.length() == 0)
        {
            us.addAll(filteredData);
        }
        else
        {
            for (User s  : filteredData)
            {
                if (s.getUname().toLowerCase(Locale.getDefault())
                        .contains(charText))
                {
                    us.add(s);
                }
            }
        }
        notifyDataSetChanged();
    }
}
