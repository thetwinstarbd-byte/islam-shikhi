/**
 * "ভুল জানান"-এর ইমেইল-গ্রহণকারী (Google Apps Script)। ফ্রি; কার্ড লাগে না।
 * সেটআপ: docs/REPORT_SETUP.md দেখুন। নিচের TOKEN-এ নিজের একটি গোপন কোড বসান (অ্যাপের REPORT_TOKEN-এর সাথে একই হতে হবে)।
 */
const TOKEN = 'এখানে-নিজের-গোপন-কোড-বসান';
const MAX_ATTACH_BYTES = 5 * 1024 * 1024;

function doPost(e) {
  try {
    const d = JSON.parse(e.postData.contents);
    if (d.token !== TOKEN) return out('denied');
    const message = String(d.message || '').slice(0, 5000);
    if (!message && !d.data) return out('empty');

    // দিনে খুব বেশি ইমেইল এলে থামিয়ে দেওয়া (স্প্যাম থেকে রক্ষা)
    const props = PropertiesService.getScriptProperties();
    const today = Utilities.formatDate(new Date(), 'Asia/Dhaka', 'yyyyMMdd');
    const key = 'count_' + today;
    const count = Number(props.getProperty(key) || 0);
    if (count >= 60) return out('limit');
    props.setProperty(key, String(count + 1));

    const attachments = [];
    if (d.data) {
      const bytes = Utilities.base64Decode(d.data);
      if (bytes.length <= MAX_ATTACH_BYTES) {
        attachments.push(Utilities.newBlob(bytes, d.mime || 'application/octet-stream', String(d.filename || 'attachment')));
      }
    }
    const body =
      'ধরন: ' + (d.category || '') + '\n' +
      'যোগাযোগ: ' + (d.contact || '(দেননি)') + '\n' +
      'অ্যাপ: ' + (d.app || '') + '\n' +
      'ফোন: ' + (d.device || '') + '\n\n' + message;
    const options = { to: Session.getEffectiveUser().getEmail(), subject: '[ইসলাম শিখি] ভুল রিপোর্ট: ' + String(d.category || '').slice(0, 40), body: body, attachments: attachments, name: 'ইসলাম শিখি অ্যাপ' };
    if (/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(String(d.contact || ''))) options.replyTo = d.contact;
    MailApp.sendEmail(options);
    return out('ok');
  } catch (err) {
    return out('error');
  }
}

function out(s) { return ContentService.createTextOutput(s); }
