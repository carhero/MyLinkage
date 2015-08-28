package com.app.dlna.dmc.processor.impl;

import android.util.Log;
import android.widget.RemoteViews;

import com.app.dlna.dmc.processor.interfaces.DMRProcessor;

import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.action.ActionException;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.support.avtransport.callback.GetPositionInfo;
import org.fourthline.cling.support.avtransport.callback.GetTransportInfo;
import org.fourthline.cling.support.avtransport.callback.Pause;
import org.fourthline.cling.support.avtransport.callback.Play;
import org.fourthline.cling.support.avtransport.callback.Seek;
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI;
import org.fourthline.cling.support.avtransport.callback.Stop;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.SeekMode;
import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.renderingcontrol.callback.GetVolume;
import org.fourthline.cling.support.renderingcontrol.callback.SetVolume;

import java.util.ArrayList;
import java.util.List;

public class DMRProcessorImpl implements DMRProcessor {
	public static final int REMOTE_DEV_MAX_VOLUME = 100;
	private static final String TAG = DMRProcessorImpl.class.getName();
	private static final int UPDATE_INTERVAL = 500;
//	private static final int ALMOST_COMPLETE_PERCENT = 97;
//	private boolean m_isAlmostCompleted = false;
	private ControlPoint m_controlPoint;
	private RemoteService m_avtransportService = null;
	private RemoteService m_renderingControl = null;

	private List<DMRProcessorListener> m_listeners = new ArrayList<DMRProcessorListener>();
	private boolean m_isRunning = true;
	private boolean m_canUpdatePosition = true;
	private boolean m_canUpdateVolume = true;
	private boolean m_isSettingURI = false;
	private boolean m_hasPendingURI = false;
    private  boolean iscompleted;
   
	private String m_latestURI = null;
	private String m_latestURIMeta = null;
    private int SLEEP=500;
	private boolean m_hasSetURI = false;
	RemoteDevice m_device;
	private boolean m_checkGetVolumeInfo = false;
	private int m_currentVolume;
	private Thread m_updateThread = new Thread(new Runnable() {

		@Override
		public void run() {
			int counter = 0;
			while (m_isRunning) {
				if (m_hasSetURI && !m_isSettingURI ) {
					getControlPointStatus(counter);

				}
				try {
					Thread.sleep(UPDATE_INTERVAL);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (m_renderingControl != null && !m_checkGetVolumeInfo) {
					m_checkGetVolumeInfo = true;
					m_controlPoint.execute(new GetVolume(m_renderingControl) {
						@SuppressWarnings("rawtypes")
						@Override
						public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
							/*fireOnFailEvent(invocation.getAction(), operation, defaultMsg);*/
							m_checkGetVolumeInfo = false;
						}

						@SuppressWarnings("rawtypes")
						@Override
						public void received(ActionInvocation actionInvocation, int currentVolume) {
							m_currentVolume = currentVolume;
							if (m_currentVolume!=0)
							fireUpdateVolumeEvent(currentVolume);
							Log.d(TAG,"current volume"+currentVolume);
							m_checkGetVolumeInfo = false;
						}
					});
				}
				
		}
		}
	});



    private Thread m_GetTransportStateThread = new Thread(new Runnable() {

        @Override
        public void run() {
            int counter = 2;
            while (m_isRunning) {
                if (m_hasSetURI && !m_isSettingURI) {
                    getControlPointStatus(counter);
                }
                try {
                    Thread.sleep(SLEEP);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    });
	public DMRProcessorImpl(RemoteDevice remoteDevice,RemoteService service, ControlPoint controlPoint) {
		m_controlPoint = controlPoint;
		m_avtransportService = service;
		
		m_device=remoteDevice;
	    m_renderingControl = m_device.findService(new ServiceType("schemas-upnp-org", "RenderingControl"));
		m_updateThread.start();
        m_GetTransportStateThread.start();

	}
	
	private void getControlPointStatus(int counter) {
		if (m_avtransportService == null)
			return;

		switch (counter) {
		case 0:
			m_controlPoint.execute(new GetPositionInfo(m_avtransportService) {
				@SuppressWarnings("rawtypes")
				@Override
				public void failure(ActionInvocation invocation, UpnpResponse response, String defaultMsg) {
					/*fireOnFailEvent(invocation.getAction(), response,
							GetPositionInfo.class.getSimpleName() + ":" + defaultMsg);*/

				}


                @SuppressWarnings("rawtypes")
				@Override
				public void received(ActionInvocation invocation, PositionInfo positionInfo) {
//					int elapsedPercent = positionInfo.getElapsedPercent();
//					Log.d(TAG, "song percent:" + elapsedPercent);
//					if (elapsedPercent > ALMOST_COMPLETE_PERCENT) {
//						m_isAlmostCompleted = true;
////						fireOnPlayCompletedEvent();
//					}
                    if (positionInfo.getElapsedPercent()>97)
                    {
                        iscompleted=true;

                    }

					fireUpdatePositionEvent(positionInfo);


				}
			});
			break;
		case 1:
			break;
		case 2:
			m_controlPoint.execute(new GetTransportInfo(m_avtransportService) {
				@SuppressWarnings("rawtypes")
				@Override
				public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
					/*fireOnFailEvent(invocation.getAction(), operation,
							GetTransportInfo.class.getSimpleName() + ":" + defaultMsg);*/
				}

				@SuppressWarnings("rawtypes")
				@Override
				public void received(ActionInvocation invocation, TransportInfo transportInfo) {
					switch (transportInfo.getCurrentTransportState()) {
					case PLAYING:
						fireOnPlayingEvent();
                        Log.d(TAG,"Fireonplayingevent");

						break;
					case PAUSED_PLAYBACK:
						fireOnPausedEvent();
                        Log.d(TAG,"Fireonpauseevent");
						break;
					case STOPPED:
						fireOnStopedEvent();
						break;
					default:
						break;
					}
				}

			});
			break;
		case 3:
			break;
		case 4:
//			m_controlPoint.execute(new GetVolume(m_renderingControl) {
//			@SuppressWarnings("rawtypes")
//			@Override
//			public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
//				fireOnFailEvent(invocation.getAction(), operation, defaultMsg);
//			}
//
//			@SuppressWarnings("rawtypes")
//			@Override
//			public void received(ActionInvocation actionInvocation, int currentVolume) {
//				if (m_lastVolume != currentVolume) {
//					m_lastVolume = currentVolume;
//					fireUpdateVolumeEvent(currentVolume);
//				}
//			}
//		});
			break;
		default:
			break;
		}
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public void setURI(final String uri,final String uriMeta) {
        fireOnSetURIEvent();
      /*  m_hasSetURI = true;
        m_latestURI = uri;
        m_latestURIMeta = uriMeta;
        if (m_isSettingURI) {
            Log.e(TAG, "set AV uri pending:" + uri);
            m_hasPendingURI = true;
            return;
        }
        Log.e(TAG, "set AV uri now:" + uri);
        stop();

        m_isSettingURI = true;
        m_hasPendingURI = false;
        m_controlPoint.execute(new SetAVTransportURI(m_avtransportService, uri, uriMeta) {
            @Override
            public void success(ActionInvocation invocation) {
                super.success(invocation);
                Log.e(TAG,"Success setting URI to DMR");
                m_isSettingURI = false;
                if (m_hasPendingURI) {
                    setURI(m_latestURI, m_latestURIMeta);
                } else {
                    play();
                }
            }

           *//* @Override
            public ActionInvocation getActionInvocation() {
                int error=getActionInvocation().getFailure().getErrorCode();

                Log.d(TAG,"actionException"+error);
                return super.getActionInvocation();


            }*//*

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse response, String defaultMsg) {
                m_isSettingURI = false;
                Log.e(TAG,"Failed to SetAVTransportURI");


                fireOnFailEvent(invocation.getAction(), response,
                        SetAVTransportURI.class.getSimpleName() + ":" + defaultMsg);
            }
        });*/


        Stop stop = new Stop(m_avtransportService) {


            @Override
            public void success(ActionInvocation invocation) {
                super.success(invocation);
                Log.d(TAG,"Stop is sucess");

                m_hasSetURI = true;
                m_latestURI = uri;
                m_latestURIMeta = uriMeta;
                if (m_isSettingURI) {
                    Log.i(TAG, "set AV uri pending:" + uri);
                    m_hasPendingURI = true;
                    return;
                }
                Log.i(TAG, "set AV uri now:" + uri);

                m_isSettingURI=true;

                m_hasPendingURI = false;
                m_controlPoint.execute(new SetAVTransportURI(m_avtransportService, uri, uriMeta) {
                    @Override
                    public void success(ActionInvocation invocation) {
                        super.success(invocation);
                        Log.d(TAG,"set uri is sucess");
                        m_isSettingURI=false;
                        iscompleted=false;
                        play();


                    }

                    @Override
                    public void failure(ActionInvocation invocation, UpnpResponse response, String defaultMsg) {
                        Log.e(TAG,"Set uri is failed");
                        m_isSettingURI=false;
                        if (response!=null) {
                            iscompleted = false;

                        //   fireOnFailEvent(invocation.getAction(),response,defaultMsg);



                        }
                        else {
                            setURI(uri,uriMeta);
                        }

                    }
                });

            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse response, String defaultMsg) {
                fireOnFailEvent(invocation.getAction(), response,
                        Stop.class.getSimpleName() + ":" + defaultMsg);
                Log.d(TAG,"Stop is sucess");

            }
        };
        fireOnSetURIEvent();

        m_controlPoint.execute(stop);

	}

	@SuppressWarnings("rawtypes")
    @Override
    public void play() {

        if (!m_hasSetURI)
            return;
        Play play = new Play(m_avtransportService) {

            @Override
            public void success(ActionInvocation invocation) {
                super.success(invocation);
                Log.d(TAG, "Play Sucess ");
                fireOnPlayingEvent();
                iscompleted=false;
            }


            @Override
            public void failure(ActionInvocation invocation, UpnpResponse response, String defaultMsg) {
                if (iscompleted) {

                }

                Log.e(TAG, "Play is failed");
                if (!iscompleted)
                {     play();
                    iscompleted=true;
                }

            }

        };

        m_controlPoint.execute(play);

    }

	@SuppressWarnings("rawtypes")
	@Override
	public void pause() {
		Pause pause = new Pause(m_avtransportService) {

			@Override
			public void success(ActionInvocation invocation) {
				super.success(invocation);
			}

			@Override
			public void failure(ActionInvocation invocation, UpnpResponse response, String defaultMsg) {
				/*fireOnFailEvent(invocation.getAction(), response,
						Pause.class.getSimpleName() + ":" + defaultMsg);*/
			}

		};
		m_controlPoint.execute(pause);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void stop() {
		Log.v(TAG,"Send Stop");
		Stop stop = new Stop(m_avtransportService) {
			@Override
			public void success(ActionInvocation invocation) {
				super.success(invocation);
			}

			@Override
			public void failure(ActionInvocation invocation, UpnpResponse response, String defaultMsg) {
				/*fireOnFailEvent(invocation.getAction(), response,
						Stop.class.getSimpleName() + ":" + defaultMsg);*/
			}
		};

		m_controlPoint.execute(stop);
	}

	@Override
	public void addListener(DMRProcessorListener listener) {
		synchronized (m_listeners) {
			m_listeners.add(listener);
		}
	}

	@Override
	public void removeListener(DMRProcessorListener listener) {
		synchronized (m_listeners) {
			m_listeners.remove(listener);
		}
	}

	@SuppressWarnings("rawtypes")
	private void fireOnFailEvent(Action action, UpnpResponse response, String message) {
		
		synchronized (m_listeners) {
			for (DMRProcessorListener listener : m_listeners) {
				listener.onActionFail(action, response, message);
				
			}
		}

	}

	private void fireUpdatePositionEvent(PositionInfo positionInfo) {
		if (m_canUpdatePosition) {
			synchronized (m_listeners) {
				for (DMRProcessorListener listener : m_listeners) {
					listener.onUpdatePosition(positionInfo.getTrackElapsedSeconds(),
							positionInfo.getTrackDurationSeconds());
				}
			}
		}
	}


	private void fireUpdateVolumeEvent(int currentVolume) {
		// TODO Auto-generated method stub
		
			synchronized (m_listeners) {
				for (DMRProcessorListener listener : m_listeners) {
					listener.onUpdateVolume(currentVolume);
				}
			}
		
	}

	private void fireOnStopedEvent() {
		synchronized (m_listeners) {
			for (DMRProcessorListener listener : m_listeners) {
				listener.onStoped();
			}
		}


		if (iscompleted)
        fireOnPlayCompletedEvent();
//		if (m_isAlmostCompleted) {
//			m_isAlmostCompleted = false;
//			fireOnPlayCompletedEvent();
//		}
	}

	private void fireOnPausedEvent() {
		synchronized (m_listeners) {
			for (DMRProcessorListener listener : m_listeners) {
				listener.onPaused();
			}
		}
	}

	private void fireOnPlayingEvent() {
		synchronized (m_listeners) {
			for (DMRProcessorListener listener : m_listeners) {
				listener.onPlaying();
			}
		}
	}
	
	private void fireOnPlayCompletedEvent() {
		synchronized (m_listeners) {
			for (DMRProcessorListener listener : m_listeners) {
				listener.onPlayCompleted();
			}
		}
	}
	
	private void fireOnSetURIEvent() {
		synchronized (m_listeners) {
			for (DMRProcessorListener listener : m_listeners) {
				listener.onSetURI();
			}
		}
	}

	@Override
	public void dispose() {
		m_isRunning = false;
		stop();
	}

	@Override
	public void seek(String position) {
		Log.d(TAG, "Call seek:" + position);
		m_canUpdatePosition = false;
		@SuppressWarnings("rawtypes")
        Seek seek = new Seek(m_avtransportService, SeekMode.REL_TIME, position) {
			@Override
			public void success(ActionInvocation invocation) {
				super.success(invocation);
				Log.d(TAG, "Seek success");
				m_canUpdatePosition = true; // changed for checking 
			}

			@Override
			public void failure(ActionInvocation invocation, UpnpResponse reponse, String defaultMsg) {
				Log.w(TAG, "Seek fail: " + defaultMsg);
				/*fireOnFailEvent(invocation.getAction(), reponse,
						Seek.class.getSimpleName() + ":" + defaultMsg);*/
			}
		};
		m_controlPoint.execute(seek);
	}

	@Override
	public void seek(long position) {
		// TODO Auto-generated method stub
		seek(ModelUtil.toTimeString(position));
	}

	@Override
	public void setVolume(int newVolume) {
		Log.d(TAG, "Call setVolume");
		m_canUpdateVolume = false;
		m_controlPoint.execute(new SetVolume(m_renderingControl, newVolume) {
			
		@SuppressWarnings("rawtypes")
			@Override
			public void success(ActionInvocation invocation) {
				// TODO Auto-generated method stub
				super.success(invocation);
				Log.d(TAG, "Seek success");
				m_canUpdateVolume = true;
			}
			@SuppressWarnings("rawtypes")
			public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
				Log.d(TAG, "setVolume fail: " + defaultMsg);
				/*fireOnFailEvent(invocation.getAction(), operation, defaultMsg);*/
			}
		});
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		m_isRunning = false;
	}

	@Override
	public int getMaxVolume() {
		// TODO Auto-generated method stub
		return REMOTE_DEV_MAX_VOLUME;
	}

	@Override
	public int getVolume() {
		// TODO Auto-generated method stub
		// simply return 0, value will be updated by socket
		return m_currentVolume;
	}
}
