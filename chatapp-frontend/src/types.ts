import {ULID} from "ulid";

export interface IUser {
  username: string;

  id: string,
  role: string,
  // contacts: IContact[];
}

export interface IContact {
  id: string;
  username: string;
  avatar?: string;

}

export interface IPreviewData {
  title: string;
  image?: string;
  description: string;
  domain: string;
  link: string;
}

export interface IAttachment {
  id: number;
  type: string;
  name: string;
  size: string;
  url: string;
  thumbnail?: string;
  file?: File;
}

export interface IRecording {
  id: number;
  size: string;
  src: string;
  duration: string;
  file?: File;
}

export interface IMessage {
  id: string | null;
  type?: string;
  content?: string | IRecording;
  date: number;
  from: string;
  to: string;
  replyTo?: string;
  previewData?: IPreviewData;
  attachments?: IAttachment[];
  media?: IMedia
  state: string;
}

export interface IConversation {
  id: string;
  type: string;
  name?: string;
  avatar?: string;
  admins?: string[];
  contacts: IContact[];
  messages: IMessage[] | undefined;
  pinnedMessage?: IMessage;
  pinnedMessageHidden?: boolean;
  replyMessage?: IMessage;
  unread?: number;
  draftMessage: string;
}

export interface IContactGroup {
  letter: string;
  contacts: IContact[];
}

export interface INotification {
  flag: string;
  title: string;
  message: string;
}

export interface ISettings {
  lastSeen: boolean;
  readReceipt: boolean;
  joiningGroups: boolean;
  privateMessages: boolean;
  darkMode: boolean;
  borderedTheme: boolean;
  allowNotifications: boolean;
  keepNotifications: boolean;
}

export interface ICall {
  type: string;
  direction: string;
  status: string;
  date: string;
  length: string;
  members: IContact[];
  adminIds: number[];
}

export interface IEmoji {
  n: string[];
  u: string;
  r?: string;
  v?: string[];
}

export interface IUserSignUp {
  username: string;
  password: string;
  passwordConfirmation: string;
}

export interface IUserLogin {
  username: string;
  password: string;
}

export interface ICreateGroup {
  name: string;
  contacts: String[]
}

export interface IMedia {
  id: string,
  name?: string,
  size: number,
  type?: string
}
export interface IEnvelope {
  from: string,
  to: string,
  type?: string;
  content?: string | IRecording;
  date: number;
  media?: IFileUpload
}

export interface IFileUpload{
  file?: File | null,
  caption: string
}