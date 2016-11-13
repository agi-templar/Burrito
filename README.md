
# Burrito

Team Member: Ruibo Liu & Tian Xia

![](https://s3.amazonaws.com/artceleration/effect.jpg)

## Development Goals

1. Swift development based on appropriate __Modularity__, including needed Abstraction and MVC project structure.
2. High-Efficiency threads control and smart service progress lifecycle design. 
3. Smooth user experience and friendly UI.
4. Several available filter, based on algorithms specially designed. 
5. Social Network features, which enable users to share their edited images on social media!

## Highly-Abstract Architecture

![](https://s3.amazonaws.com/artceleration/Ass2.png)

Above is the project structure in high level. All activities are marked with circles and classes are marked with rounded rectangles. We could see that ArtLib is the core for the whole structure. Besides the helper classes and needed supported classes provided by professor, I created three other classes to implement service and thread. 

## Message Queue & Separate Thread… How does it work? 

![](https://s3.amazonaws.com/artceleration/truck_whole.png)

Above is a big picture of the whole story. We assumes __the artTransformService__ to be a burrito making service. For convenience, we provide a truck for this service, such as we provide our __artTransformService__ with a __separate thread__. The process of making burrito is like a __runnable__ event, and instead of doing by ourselves, we hire a guy to handle all the burrito things, like receiving orders and making burritos. 

“Receiving orders” is just like “receiving message”. __Message__, in our app, is sent when the ArtLib needs the __artTransformService__ (with all the data). Similarly, in the burrito truck, the cook receives orders/messages from the customers who need this snack service. A better way to handle these trivial orders is to hire another guy who can help us loop through the order in a FIFO manner, as professor mentioned.

![](https://s3.amazonaws.com/artceleration/truck.png)

Now, we hire a girl to help us do the “Looper” job. Her name is “__Looper__”. What she does is: Every time she notices the cook is about to finish an order, she grabs the next order from the “__Message Queue__” and gives it to the cook. That’s basically a __FIFO manner__, because of the existing of message pool.

## Further, AsyncTask and ThreadPool

After we figure out how the combination of thread, looper and handler works, we need to think about how to imporve the efficiency of our app, especially because all the ArtTransform should run without disturbing each other, which means we need to use parallel threads.

A thread pool is a good idea. Android provides some defined threadpools, like "Executors.newCachedThreadPool()" or "Executors.newFixedThreadPool()". More detailed information could be found [here](https://developer.android.com/reference/java/util/concurrent/Executors.html).

Another thing we need think a little bit is how to queue how task. We may make several ArtTransform requests at the same time, or at least, due to the processing time, there would be some requests processing simultaneously.
Android also introduces some great tools for us to handle the multi-thread task. It's called "__AsyncTask__".

>**Definition from Android Docs**
AsyncTask is designed to be a helper class around Thread and Handler and does not constitute a generic threading framework. AsyncTasks should ideally be used for short operations (a few seconds at the most.) If you need to keep threads running for long periods of time, it is highly recommended you use the various APIs provided by the java.util.concurrent package such as Executor, ThreadPoolExecutor and FutureTask.

So my strategy is, everytime the service receives message from the client (here, it's ArtLib.class.), it would call a AsyncTask, where we retrieve data from message, do the transform and send back the message including the proceeded image to the client. The client would implement the listener method of activity which would notify the main activity to update its UI elements.  

## Corresponding Code Strategy
### ArtTransformService
```java
	public class ArtTransformService extends Service{

    private static final String TAG = ArtTransformService.class.getSimpleName();
    
    public Messenger mMessenger = new Messenger(new ArtTransformHandler());
    private ArtTransformHandler mArtTransformHandler;
    public ArtTransformService() {
    }


    @Override
    public void onCreate() {

        // Put service into a separate Thread named "ArtTransformThread"
        ArtTransformThread thread = new ArtTransformThread();
        thread.setName("ArtTransformThread");
        thread.start();

        // Make sure Handler is available
        while (thread.mArtTransformHandler == null) {

        }
        mArtTransformHandler = thread.mArtTransformHandler;
        mArtTransformHandler.setService(this);
    }
    ...
```

In @Overriding OnCreat() Method, we initiate a separate thread for our artTransformService. The while() {} loop is needed because we should make sure out handler is available before we give the reference to the handler to the thread class.

### ArtTransformThread
```java
public class ArtTransformThread extends Thread{
    private static final String TAG = ArtTransformThread.class.getSimpleName();
    public ArtTransformHandler mArtTransformHandler;

    @Override
    public void run() {
        Looper.prepare();
        mArtTransformHandler = new ArtTransformHandler();
        Looper.loop();
    }
}
```

Nothing fancy. Actually there are two ways to create a separate thread. Besides above, we could also announce a new thread in the Manifest file.

### ArtTransformHandler
```java
public class ArtTransformHandler extends Handler {
    private ArtTransformService mService;
    static ArrayList<Messenger> mClients = new ArrayList<>();
    static Messenger targetMessenger;
    List<ArtTransformAsyncTask> mArtTransformAsyncTasks;


    @Override
    public void handleMessage(Message msg) {

        targetMessenger = msg.replyTo;
        mArtTransformAsyncTasks = new ArrayList<>();

        switch (msg.what) {
            case 0:
                Log.d("doTransform", "Gaussian_Blur");

                try {
                    new ArtTransformAsyncTask().executeOnExecutor(Executors.newCachedThreadPool(), loadImage(msg));
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    Log.d("AsyncTask", "Gaussian_Blur Finished");
                }

                break;
            ...
            default:
                break;
        }

    }
     ...
```
Here we handle the message from the client, and based on "msg.what", we start different AsyncTask. The point is, we use ThreadPool to handle all the Asynctasks, which could definitely speed up our processing.

### ArtTransformAsyncTask
```java
public class ArtTransformAsyncTask extends AsyncTask<Bitmap, Void, Void> {

        private Bitmap rawBitmap;

        @Override
        protected void onPreExecute() {
            mArtTransformAsyncTasks.add(this);
        }

        @Override
        protected Void doInBackground(Bitmap... params) {
            rawBitmap = changeLight(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mArtTransformAsyncTasks.remove(this);
            if (mArtTransformAsyncTasks.size() == 0) {
                Log.d("AsyncTask", "All Tasks Finished");
            }
            notifyArtLib(rawBitmap);
        }
    }
```
and mentioned methods partly are:
```java
private Bitmap changeLight(Bitmap img) {
        ColorMatrix colorMatrixchangeLight = new ColorMatrix();
        ColorMatrix allColorMatrix = new ColorMatrix();

        colorMatrixchangeLight.reset();
        colorMatrixchangeLight.setScale(1.5f, 1.5f, 1.5f, 1);

        allColorMatrix.reset();
        allColorMatrix.postConcat(colorMatrixchangeLight);

        Bitmap newBitmap = Bitmap.createBitmap(img.getWidth(), img.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(newBitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        paint.setColorFilter(new ColorMatrixColorFilter(allColorMatrix));
        canvas.drawBitmap(img, 0, 0, paint);
        return newBitmap;
    }
```
Above is one of three my custom filters. This one could change the brightness of the image. 
## Challenge

In fact, there's nothing too challenging. Maybe the debug progress is something frustrating.

![](https://s3.amazonaws.com/artceleration/threadpool.png)

That is DDMS to make sure we manage to create a separate thread.

![](https://s3.amazonaws.com/artceleration/FIFO.png)

Above is debug log. Click different option could send different message to handler. After processing, the onTransformProcessed() Method would be triggered.

## Improvement/Potential Extra Credits 

I think we could do something more on the service connection management, because service also has its lifecycle. If we do not consider about when the service should connect, when disconnect, it would always stay in our users' background progresses and consume device resource all the time. 

So we have tried some mechanisms like:

```java
...
@Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Message message = Message.obtain();
        message.arg1 = startId;
        mArtTransformHandler.sendMessage(message);
        return Service.START_REDELIVER_INTENT;
    }
...
```

We assume value of "arg1" in message to be starId, which is a parameter recording the unique ID of service progress everytime the service is called.

```java
...
@Override
    public void handleMessage(Message msg) {
        
        ...

        // Stop the service progress based on unique ID 
        mService.stopSelf(msg.arg1);
    }
...
```

Above is in the Handler class. It enables out service progress could stop by itself after all its message queue has been handled yet. The keypoint is, if our user kill the service progress for some reasons, the service would automatically continue its undo queue handle job!

What's more, I have already done three custom effects right now (as shown at the beginning). I think finally I could develop more interesting effects!

That's all. Really thanks professor. It’s a great experience.


