package app.com.mychat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiTextView;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatListAdapter extends ArrayAdapter<GetData>
{
    private String mUserId;
    Context context;
    private static LayoutInflater inflater = null;
    private SimpleDateFormat dateFormat;
    SharedPreferences sharedpreferences ;//= getSharedPreferences("pref",Context.MODE_PRIVATE);
    public ImageLoader imageLoader;
    private SparseBooleanArray mSelectedItemsIds;
    List<GetData> messages;
    public ArrayList<Integer> selectedIds;
    ListView listView;

    // 8401470162

    public ChatListAdapter(Context context, String userId, List<GetData> messages,ListView listView)
    {
        super(context, 0, messages);
        sharedpreferences = context.getSharedPreferences("pref",Context.MODE_PRIVATE);;
        this.context = context;
        this.mUserId = userId;
        this.messages=messages;
        mUserId = sharedpreferences.getString("mymobile","");
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader=new ImageLoader(context.getApplicationContext());
        mSelectedItemsIds = new  SparseBooleanArray();
        selectedIds = new ArrayList<Integer>();
        this.listView=listView;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        convertView=null;
        if (convertView == null)
        {
            convertView = LayoutInflater.from(getContext()).
            inflate(R.layout.chatsinglemessage, parent, false);
            final ViewHolder holder = new ViewHolder();
            dateFormat = new SimpleDateFormat("hh:mm aa", Locale.US);
            holder.body = (EmojiTextView)convertView.findViewById(R.id.singleMessage);
            holder.imgMsg=(ImageView)convertView.findViewById(R.id.imageView);
            holder.imgUser=(ImageView)convertView.findViewById(R.id.imgUserLogo);
            holder.time=(TextView)convertView.findViewById(R.id.textTime);
            holder.MessageContainer = (LinearLayout)convertView.findViewById(R.id.singleMessageContainer);
            holder.imgContainer = (LinearLayout)convertView.findViewById(R.id.imgContainer);
            holder.imgCheck = (ImageView)convertView.findViewById(R.id.imgCheck);
            holder.progress = (ProgressBar)convertView.findViewById(R.id.progressImage);
            holder.videoView = (ImageView) convertView.findViewById(R.id.video);
            holder.vdo_icon = (ImageView) convertView.findViewById(R.id.video_icon);
            holder.audio = (ImageView) convertView.findViewById(R.id.audio);

            holder.progress.setVisibility(View.GONE);


            if (selectedIds.contains(position))
            {
                convertView.setSelected(true);
                convertView.setPressed(true);
                convertView.setBackgroundColor(Color.parseColor("#ff8fb5"));
            }
            else
            {
                convertView.setSelected(false);
                convertView.setPressed(false);
                convertView.setBackgroundColor(Color.parseColor("#00000000"));
            }
            convertView.setTag(holder);
        }
        final GetData message = (GetData)getItem(position);
        final ViewHolder holder = (ViewHolder)convertView.getTag();
        final boolean isMe = message.getUid().equals(mUserId);

        if (message.status)
        {
            if(message.getType().equals("Text") || message.getType().equals("text"))
            {
                holder.imgCheck.setVisibility(View.VISIBLE);
                holder.imgMsg.setVisibility(View.GONE);
                holder.videoView.setVisibility(View.GONE);
                holder.vdo_icon.setVisibility(View.GONE);
                holder.audio.setVisibility(View.GONE);


                String fromServerUnicodeDecoded = null;
                try
                {
                    fromServerUnicodeDecoded = StringEscapeUtils.unescapeJava(URLDecoder.decode(message.getBody(), "UTF-8"));
                }
                catch (UnsupportedEncodingException e)
                {
                    e.printStackTrace();
                    System.out.println("the emojis decode exe :" + e);
                }
               // System.out.println("the emojis decode is :" + fromServerUnicodeDecoded);

                holder.body.setText(fromServerUnicodeDecoded);

                holder.body.setText(fromServerUnicodeDecoded);
                holder.time.setText(message.getMsgtime());
                holder.body.setBackgroundResource(R.drawable.bubbleright);
                holder.MessageContainer.setGravity(Gravity.RIGHT);
                holder.time.setGravity(Gravity.RIGHT);
                holder.imgContainer.setGravity(Gravity.RIGHT);

                Picasso.with(context).load(sharedpreferences.getString("myImage","")).fit().placeholder(R.drawable.img_pic).transform(new CircleTransform()).into(holder.imgUser);


                if(message.getDelever().equals("1"))
                {
                    holder.imgCheck.setImageResource(R.drawable.check_one);
                }
                else if(message.getDelever().equals("2"))
                {
                     holder.imgCheck.setImageResource(R.drawable.check_two);
                }
                else if(message.getDelever().equals("f"))
                {
                     holder.imgCheck.setImageResource(R.drawable.check_two);
                }

                if(message.getRead().equals("read"))
                {
                    holder.imgCheck.setImageResource(R.drawable.check_three);
                }
                else
                {
                    //holder.imgCheck.setImageResource(R.drawable.check_two);
                }
            }
            else if(message.getType().equals("image"))
            {
                holder.imgCheck.setVisibility(View.VISIBLE);
                holder.body.setVisibility(View.GONE);
                holder.videoView.setVisibility(View.GONE);
                holder.vdo_icon.setVisibility(View.GONE);

                holder.imgMsg.setBackgroundResource(R.drawable.bubbleright);
                holder.time.setText(message.getMsgtime());
                holder.MessageContainer.setGravity(Gravity.RIGHT);
                holder.time.setGravity(Gravity.RIGHT);
                holder.imgContainer.setGravity(Gravity.RIGHT);
                holder.imgMsg.setVisibility(View.GONE);
                holder.progress.setVisibility(View.VISIBLE);


               // imageLoader.DisplayImage(message.getImgLink(),holder.imgMsg);

                 imageLoader.DisplayImage(message.getBody(),holder.imgMsg);

                //Picasso.with(context).load(message.getImgLink()).placeholder(R.drawable.image_show).into(holder.imgMsg);

               /*  Glide.with(context).load(message.getImgLink())
                        .thumbnail(0.5f)
                        .crossFade()
                        .placeholder(R.drawable.image_show)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.imgMsg);
              */
                Picasso.with(context).load(sharedpreferences.getString("myImage","")).placeholder(R.drawable.img_pic).transform(new CircleTransform()).into(holder.imgUser);

                if(message.getDelever().equals("1"))
                {
                    holder.imgCheck.setImageResource(R.drawable.check_one);
                }
                else if(message.getDelever().equals(2))
                {
                    holder.imgCheck.setImageResource(R.drawable.check_two);
                    holder.imgMsg.setVisibility(View.VISIBLE);
                    holder.progress.setVisibility(View.GONE);
                }
                else
                {
                    holder.imgMsg.setVisibility(View.VISIBLE);
                    holder.progress.setVisibility(View.GONE);
                    holder.imgCheck.setImageResource(R.drawable.check_three);
                }
                if(message.getRead().equals("read"))
                {
                    holder.imgCheck.setImageResource(R.drawable.check_three);
                }
                else
                {
                    //holder.imgCheck.setImageResource(R.drawable.check_two);
                }
            }

            else if(message.getType().equals("video"))
            {
                holder.imgCheck.setVisibility(View.VISIBLE);
                holder.body.setVisibility(View.GONE);
                holder.videoView.setVisibility(View.GONE);
                holder.vdo_icon.setVisibility(View.GONE);
                holder.audio.setVisibility(View.GONE);
                holder.imgMsg.setVisibility(View.GONE);
                holder.videoView.setBackgroundResource(R.drawable.bubbleright);
                holder.time.setText(message.getMsgtime());
                holder.MessageContainer.setGravity(Gravity.RIGHT);
                holder.time.setGravity(Gravity.RIGHT);
                holder.imgContainer.setGravity(Gravity.RIGHT);
                holder.progress.setVisibility(View.VISIBLE);

               // Picasso.with(context).load(message.getImgLink()).placeholder(R.drawable.play_video).into(holder.videoView);

                Glide.with(context).load(message.getImgLink())
                        .thumbnail(0.5f)
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.circle_shap_trans)
                        .into(holder.videoView);

                 //  System.out.println("the bitmap is 1:" + message.getBitmap());
                 //  holder.videoView.setImageBitmap(message.getBitmap());
                 //  imageLoader.DisplayImage(message.getImgLink(), holder.videoView);


                Picasso.with(context).load(sharedpreferences.getString("myImage","")).placeholder(R.drawable.img_pic).transform(new CircleTransform()).into(holder.imgUser);
                if(message.getDelever().equals("1"))
                {
                    holder.imgCheck.setImageResource(R.drawable.check_one);
                }
                else if(message.getDelever().equals(2))
                {
                    holder.imgCheck.setImageResource(R.drawable.check_two);
                    holder.progress.setVisibility(View.GONE);
                    holder.videoView.setVisibility(View.VISIBLE);
                    holder.vdo_icon.setVisibility(View.VISIBLE);
                }
                else
                {
                    holder.progress.setVisibility(View.GONE);
                    holder.videoView.setVisibility(View.VISIBLE);
                    holder.vdo_icon.setVisibility(View.VISIBLE);
                    holder.imgCheck.setImageResource(R.drawable.check_three);
                }
            }

            else if(message.getType().equals("audio"))
            {
                holder.imgCheck.setVisibility(View.VISIBLE);
                holder.body.setVisibility(View.GONE);
                holder.videoView.setVisibility(View.GONE);
                holder.vdo_icon.setVisibility(View.GONE);
                holder.audio.setVisibility(View.VISIBLE);
                holder.imgMsg.setVisibility(View.GONE);
                holder.audio.setBackgroundResource(R.drawable.bubbleright);
                holder.time.setText(message.getMsgtime());
                holder.MessageContainer.setGravity(Gravity.RIGHT);
                holder.time.setGravity(Gravity.RIGHT);
                holder.imgContainer.setGravity(Gravity.RIGHT);
                holder.progress.setVisibility(View.VISIBLE);

                // Picasso.with(context).load(message.getImgLink()).placeholder(R.drawable.play_video).into(holder.videoView);

                Glide.with(context).load(message.getImgLink())
                        .thumbnail(0.5f)
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.audio)
                        .into(holder.audio);

                //   System.out.println("the bitmap is 1:" + message.getBitmap());
                //   holder.videoView.setImageBitmap(message.getBitmap());
                //  imageLoader.DisplayImage(message.getImgLink(), holder.videoView);


                Picasso.with(context).load(sharedpreferences.getString("myImage","")).placeholder(R.drawable.img_pic).transform(new CircleTransform()).into(holder.imgUser);
                if(message.getDelever().equals("1"))
                {
                    holder.imgCheck.setImageResource(R.drawable.check_one);
                }
                else if(message.getDelever().equals(2))
                {
                    holder.imgCheck.setImageResource(R.drawable.check_two);
                    holder.progress.setVisibility(View.GONE);
                    holder.videoView.setVisibility(View.GONE);
                    holder.vdo_icon.setVisibility(View.GONE);
                    holder.audio.setVisibility(View.VISIBLE);
                }
                else
                {
                    holder.progress.setVisibility(View.GONE);
                    holder.videoView.setVisibility(View.GONE);
                    holder.audio.setVisibility(View.VISIBLE);
                    holder.vdo_icon.setVisibility(View.GONE);
                    holder.imgCheck.setImageResource(R.drawable.check_three);
                }

                if(message.getRead().equals("read"))
                {
                    holder.imgCheck.setImageResource(R.drawable.check_three);
                }
                else
                {
                    holder.imgCheck.setImageResource(R.drawable.check_two);
                }
            }
            else if(message.getType().equals("pdf"))
            {
                holder.imgCheck.setVisibility(View.VISIBLE);
                holder.body.setVisibility(View.GONE);
                holder.videoView.setVisibility(View.GONE);
                holder.vdo_icon.setVisibility(View.GONE);
                holder.audio.setVisibility(View.VISIBLE);
                holder.imgMsg.setVisibility(View.GONE);
                holder.audio.setBackgroundResource(R.drawable.bubbleright);
                holder.time.setText(message.getMsgtime());
                holder.MessageContainer.setGravity(Gravity.RIGHT);
                holder.time.setGravity(Gravity.RIGHT);
                holder.imgContainer.setGravity(Gravity.RIGHT);
                holder.progress.setVisibility(View.VISIBLE);

                // Picasso.with(context).load(message.getImgLink()).placeholder(R.drawable.play_video).into(holder.videoView);

                Glide.with(context).load(message.getImgLink())
                        .thumbnail(0.5f)
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.pdf)
                        .into(holder.audio);

                // holder.videoView.setImageBitmap(message.getBitmap());
                // imageLoader.DisplayImage(message.getImgLink(), holder.videoView);

                Picasso.with(context).load(sharedpreferences.getString("myImage","")).placeholder(R.drawable.img_pic).transform(new CircleTransform()).into(holder.imgUser);
                if(message.getDelever().equals("1"))
                {
                    holder.imgCheck.setImageResource(R.drawable.check_one);
                }
                else if(message.getDelever().equals(2))
                {
                    holder.imgCheck.setImageResource(R.drawable.check_two);
                    holder.progress.setVisibility(View.GONE);
                    holder.videoView.setVisibility(View.GONE);
                    holder.vdo_icon.setVisibility(View.GONE);
                    holder.audio.setVisibility(View.VISIBLE);
                }
                else
                {
                    holder.progress.setVisibility(View.GONE);
                    holder.videoView.setVisibility(View.GONE);
                    holder.audio.setVisibility(View.VISIBLE);
                    holder.vdo_icon.setVisibility(View.GONE);
                    holder.imgCheck.setImageResource(R.drawable.check_three);
                }

                if(message.getRead().equals("read"))
                {
                    holder.imgCheck.setImageResource(R.drawable.check_three);
                }
                else
                {
                    holder.imgCheck.setImageResource(R.drawable.check_two);
                }
            }

            else
            {
                holder.imgMsg.setVisibility(View.GONE);
                System.out.print("the Empty message called chat list adapter");
                Log.d("Logout","Empty String ");
                holder.body.setText(message.getBody() + "");
                holder.imgContainer.setVisibility(View.GONE);
            }
        }
        else
        {
            holder.imgCheck.setVisibility(View.GONE);
            if(message.getType().equals("Text"))
            {
                holder.imgMsg.setVisibility(View.GONE);
                holder.videoView.setVisibility(View.GONE);
                holder.vdo_icon.setVisibility(View.GONE);
                holder.audio.setVisibility(View.GONE);

                Picasso.with(context).load(sharedpreferences.getString("userImg","")).placeholder(R.drawable.img_pic).transform(new CircleTransform()).into(holder.imgUser);
                String fromServerUnicodeDecoded = null;
                try {
                    fromServerUnicodeDecoded = StringEscapeUtils.unescapeJava(URLDecoder.decode(message.getBody(), "UTF-8"));
                } catch (UnsupportedEncodingException e)
                {
                    e.printStackTrace();

                    System.out.println("the emojis decode exe :" + e);

                }
                // System.out.println("the emojis decode is :" + fromServerUnicodeDecoded);

                holder.body.setText(fromServerUnicodeDecoded);
                holder.time.setText(message.getMsgtime());
                holder.body.setBackgroundResource(R.drawable.bubbleleft);
                holder.MessageContainer.setGravity(Gravity.LEFT);
                holder.time.setGravity(Gravity.LEFT);
                holder.imgContainer.setGravity(Gravity.LEFT);
            }
            else if(message.getType().equals("image"))
            {
                holder.body.setVisibility(View.GONE);
                holder.videoView.setVisibility(View.GONE);
                holder.vdo_icon.setVisibility(View.GONE);
                holder.audio.setVisibility(View.GONE);

                //imageLoader.DisplayImage(message.getImgLink(),holder.imgMsg);

                 imageLoader.DisplayImage(message.getBody(), holder.imgMsg);
               /*
               Glide.with(context).load(message.getImgLink())
                        .thumbnail(0.5f)
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.image_show)
                        .into(holder.imgMsg);
               */
                //Picasso.with(context).load(message.getImgLink()).placeholder(R.drawable.image_show).into(holder.imgMsg);

                Picasso.with(context).load(sharedpreferences.getString("userImg","")).fit().placeholder(R.drawable.img_pic).transform(new CircleTransform()).into(holder.imgUser);
                holder.imgMsg.setBackgroundResource(R.drawable.bubbleleft);
                holder.time.setText(message.getMsgtime());
                holder.MessageContainer.setGravity(Gravity.LEFT);
                holder.time.setGravity(Gravity.LEFT);
                holder.imgContainer.setGravity(Gravity.LEFT);

             }
            else if(message.getType().equals("video"))
            {
                holder.body.setVisibility(View.GONE);
                holder.videoView.setVisibility(View.VISIBLE);
                holder.imgMsg.setVisibility(View.GONE);
                holder.audio.setVisibility(View.GONE);

                holder.vdo_icon.setVisibility(View.VISIBLE);
                holder.videoView.setBackgroundResource(R.drawable.bubbleleft);

                //  Picasso.with(context).load(message.getImgLink()).placeholder(R.drawable.img_pic).into(holder.videoView);

                Glide.with(context).load(message.getImgLink())
                        .thumbnail(0.5f)
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.circle_shap_trans)
                        .into(holder.videoView);

                // imageLoader.DisplayImage(message.getImgLink(), holder.videoView);

                 //holder.videoView.setImageBitmap(message.getBitmap());

                Picasso.with(context).load(sharedpreferences.getString("userImg","")).fit().placeholder(R.drawable.img_pic).transform(new CircleTransform()).into(holder.imgUser);
                holder.time.setText(message.getMsgtime());
                holder.MessageContainer.setGravity(Gravity.LEFT);
                holder.time.setGravity(Gravity.LEFT);
                holder.imgContainer.setGravity(Gravity.LEFT);
             }
            else if(message.getType().equals("audio"))
            {
                holder.body.setVisibility(View.GONE);
                holder.videoView.setVisibility(View.GONE);
                holder.imgMsg.setVisibility(View.GONE);
                holder.vdo_icon.setVisibility(View.GONE);
                holder.audio.setBackgroundResource(R.drawable.bubbleleft);
                holder.audio.setVisibility(View.VISIBLE);

                //  Picasso.with(context).load(message.getImgLink()).placeholder(R.drawable.img_pic).into(holder.videoView);

                Glide.with(context).load(message.getImgLink())
                        .thumbnail(0.5f)
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.audio)
                        .into(holder.audio);

                // imageLoader.DisplayImage(message.getImgLink(), holder.videoView);
                // holder.videoView.setImageBitmap(message.getBitmap());

                Picasso.with(context).load(sharedpreferences.getString("userImg","")).fit().placeholder(R.drawable.img_pic).transform(new CircleTransform()).into(holder.imgUser);
                holder.time.setText(message.getMsgtime());
                holder.MessageContainer.setGravity(Gravity.LEFT);
                holder.time.setGravity(Gravity.LEFT);
                holder.imgContainer.setGravity(Gravity.LEFT);
            }
            else if(message.getType().equals("pdf"))
            {
                holder.body.setVisibility(View.GONE);
                holder.videoView.setVisibility(View.GONE);
                holder.imgMsg.setVisibility(View.GONE);
                holder.vdo_icon.setVisibility(View.GONE);
                holder.audio.setBackgroundResource(R.drawable.bubbleleft);
                holder.audio.setVisibility(View.VISIBLE);

                //  Picasso.with(context).load(message.getImgLink()).placeholder(R.drawable.img_pic).into(holder.videoView);

                Glide.with(context).load(message.getImgLink())
                        .thumbnail(0.5f)
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.pdf)
                        .into(holder.audio);

                // imageLoader.DisplayImage(message.getImgLink(), holder.videoView);
                // holder.videoView.setImageBitmap(message.getBitmap());

                Picasso.with(context).load(sharedpreferences.getString("userImg","")).fit().placeholder(R.drawable.img_pic).transform(new CircleTransform()).into(holder.imgUser);
                holder.time.setText(message.getMsgtime());
                holder.MessageContainer.setGravity(Gravity.LEFT);
                holder.time.setGravity(Gravity.LEFT);
                holder.imgContainer.setGravity(Gravity.LEFT);
            }

            else
            {
              /*
                holder.imgMsg.setVisibility(View.GONE);
                System.out.println("the Empty message called chat list adapter");
                holder.body.setText(message.getBody() + "null");
                holder.imgContainer.setVisibility(View.GONE);
              */
            }
        }

       /* convertView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String t = message.getType();
                if(t.equals("Text")||t.equals("text"))
                    System.out.println("the link is  "+ message.getBody());
                else if(t.equals("image"))
                {
                    // System.out.println("the link is  " + message.getImgLink());
                    // System.out.println("the thumb is  " + message.getBody());

                    Intent fullScreenIntent = new Intent(view.getContext(), FullScreenImage.class);
                    fullScreenIntent.putExtra("image", ""+message.getImgLink());
                    fullScreenIntent.putExtra("thumb", ""+message.getBody());
                    context.startActivity(fullScreenIntent);
                }
                else if(t.equals("video"))
                {
                    Intent i = new Intent(context,VideoActivity.class);
                    i.putExtra("vdo",message.getBody());
                    context.startActivity(i);
                }
                else if(t.equals("audio"))
                {
                   // System.out.println("the vv audio is  "+ message.getBody());
                    Intent i = new Intent(context,AudioSelectActivity.class);
                    i.putExtra("audio",message.getBody());
                    context.startActivity(i);
                }
                else
                {
                    //System.out.println("the vv pdf is  "+ message.getBody());
                    Intent i = new Intent(context,FDFOpen.class);
                    i.putExtra("pdf",message.getBody());
                    context.startActivity(i);
                }
             }
        });
       */ return convertView;
    }
    final class ViewHolder
    {
        public EmojiTextView body;

        public ImageView videoView,vdo_icon,audio;
        public TextView time;
        public LinearLayout MessageContainer,imgContainer;
        public ImageView imgMsg,imgUser,imgCheck;
        public ProgressBar progress;
    }
    @Override
    public void remove(GetData object)
    {
        messages.remove(object);
        selectedIds.clear();
        notifyDataSetChanged();
    }

    public List<GetData> getWorldPopulation()
    {
        return messages;
    }

    public void toggleSelection(int position)
    {
        selectView(position, !mSelectedItemsIds.get(position));
    }

    public void removeSelection()
    {
        mSelectedItemsIds = new SparseBooleanArray();
        System.out.println("the remove Selection");
        selectedIds.clear();
        notifyDataSetChanged();
    }

    public void selectView(int position, boolean value)
    {
         if(value)
        {
            mSelectedItemsIds.put(position, value);
        }
        else
        {
            mSelectedItemsIds.delete(position);
        }
    }

    public int getSelectedCount()
    {
        return mSelectedItemsIds.size();
    }

    public SparseBooleanArray getSelectedIds()
    {
        return mSelectedItemsIds;
    }
}